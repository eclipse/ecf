/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.provider.jmdns.container;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.InetAddress;
import java.net.URI;
import java.util.*;
import javax.jmdns.*;
import javax.jmdns.ServiceInfo;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.events.*;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.discovery.*;
import org.eclipse.ecf.discovery.identity.*;
import org.eclipse.ecf.discovery.service.IDiscoveryService;
import org.eclipse.ecf.internal.provider.jmdns.*;
import org.eclipse.ecf.provider.jmdns.identity.JMDNSNamespace;

public class JMDNSDiscoveryContainer extends AbstractDiscoveryContainerAdapter implements IDiscoveryService, ServiceListener, ServiceTypeListener {

	private static final String SCHEME_PROPERTY = "jmdns.ptcl"; //$NON-NLS-1$

	public static final int DEFAULT_REQUEST_TIMEOUT = 3000;

	private static int instanceCount = 0;

	private InetAddress intf = null;
	JmDNS jmdns = null;
	private ID targetID = null;

	List serviceTypes = null;

	boolean disposed = false;
	Object lock = new Object();

	SimpleFIFOQueue queue = null;
	Thread notificationThread = null;

	public JMDNSDiscoveryContainer(InetAddress addr) throws IDCreateException {
		super(JMDNSNamespace.NAME, new DiscoveryContainerConfig(IDFactory.getDefault().createStringID(JMDNSDiscoveryContainer.class.getName() + ";" + addr.toString() + ";" + instanceCount++))); //$NON-NLS-1$  //$NON-NLS-2$
		intf = addr;
		serviceTypes = Collections.synchronizedList(new ArrayList());
	}

	/****************** IContainer methods **************************/

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#getConnectedID()
	 */
	public ID getConnectedID() {
		return this.targetID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.AbstractDiscoveryContainerAdapter#dispose()
	 */
	public void dispose() {
		synchronized (lock) {
			super.dispose();
			disposed = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#connect(org.eclipse.ecf.core.identity.ID, org.eclipse.ecf.core.security.IConnectContext)
	 */
	public void connect(ID targetID1, IConnectContext joinContext) throws ContainerConnectException {
		synchronized (lock) {
			if (disposed)
				throw new ContainerConnectException(Messages.JMDNSDiscoveryContainer_EXCEPTION_CONTAINER_DISPOSED);
			if (this.targetID != null)
				throw new ContainerConnectException(Messages.JMDNSDiscoveryContainer_EXCEPTION_ALREADY_CONNECTED);
			this.targetID = (targetID1 == null) ? getConfig().getID() : targetID1;
			fireContainerEvent(new ContainerConnectingEvent(this.getID(), this.targetID, joinContext));
			initializeQueue();
			try {
				this.jmdns = new JmDNS(intf);
				jmdns.addServiceTypeListener(this);
			} catch (final IOException e) {
				if (this.jmdns != null) {
					jmdns.close();
					jmdns = null;
				}
				throw new ContainerConnectException(Messages.JMDNSDiscoveryContainer_EXCEPTION_CREATE_JMDNS_INSTANCE);
			}
			fireContainerEvent(new ContainerConnectedEvent(this.getID(), this.targetID));
		}
	}

	private void initializeQueue() {
		queue = new SimpleFIFOQueue();
		notificationThread = new Thread(new Runnable() {
			public void run() {
				for (;;) {
					if (Thread.currentThread().isInterrupted())
						break;
					Runnable runnable = (Runnable) queue.dequeue();
					if (Thread.currentThread().isInterrupted() || runnable == null)
						break;
					try {
						runnable.run();
					} catch (Throwable t) {
						handleRuntimeException(t);
					}
				}
			}
		}, "JMDNS Discovery Thread"); //$NON-NLS-1$
		notificationThread.start();
	}

	protected void handleRuntimeException(Throwable t) {
		// Nothing to do except log
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#disconnect()
	 */
	public void disconnect() {
		synchronized (lock) {
			if (this.jmdns != null) {
				ID connectedID = getConnectedID();
				fireContainerEvent(new ContainerDisconnectingEvent(this.getID(), connectedID));
				jmdns.close();
				jmdns = null;
				queue.close();
				notificationThread = null;
				this.targetID = null;
				fireContainerEvent(new ContainerDisconnectedEvent(this.getID(), connectedID));
			}
		}
	}

	/************************* IDiscoveryContainerAdapter methods *********************/

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServiceInfo(org.eclipse.ecf.discovery.identity.IServiceID)
	 */
	public IServiceInfo getServiceInfo(IServiceID service) {
		Assert.isNotNull(service);
		synchronized (lock) {
			if (jmdns != null) {
				try {
					ServiceInfo serviceInfo = jmdns.getServiceInfo(service.getServiceTypeID().getInternal(), service.getServiceName());
					return (serviceInfo == null) ? null : createIServiceInfoFromServiceInfo(serviceInfo);
				} catch (Exception e) {
					Trace.catching(JMDNSPlugin.PLUGIN_ID, JMDNSDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), "getServiceInfo", e); //$NON-NLS-1$
					return null;
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServices()
	 */
	public IServiceInfo[] getServices() {
		synchronized (lock) {
			IServiceTypeID[] serviceTypeArray = getServiceTypes();
			List results = new ArrayList();
			for (int i = 0; i < serviceTypeArray.length; i++) {
				results.addAll(Arrays.asList(getServices(serviceTypeArray[i])));
			}
			return (IServiceInfo[]) results.toArray(new IServiceInfo[] {});
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServices(org.eclipse.ecf.discovery.identity.IServiceTypeID)
	 */
	public IServiceInfo[] getServices(IServiceTypeID type) {
		Assert.isNotNull(type);
		List serviceInfos = new ArrayList();
		synchronized (lock) {
			if (serviceTypes.contains(type)) {
				ServiceInfo[] infos = jmdns.list(type.getInternal());
				for (int i = 0; i < infos.length; i++) {
					try {
						serviceInfos.add(createIServiceInfoFromServiceInfo(infos[i]));
					} catch (Exception e) {
						Trace.catching(JMDNSPlugin.PLUGIN_ID, JMDNSDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), "getServices", e); //$NON-NLS-1$
					}
				}
			}
			return (IServiceInfo[]) serviceInfos.toArray(new IServiceInfo[] {});
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServiceTypes()
	 */
	public IServiceTypeID[] getServiceTypes() {
		return (IServiceTypeID[]) serviceTypes.toArray(new IServiceTypeID[] {});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#registerService(org.eclipse.ecf.discovery.IServiceInfo)
	 */
	public void registerService(IServiceInfo serviceInfo) throws ECFException {
		Assert.isNotNull(serviceInfo);
		synchronized (lock) {
			if (jmdns == null)
				throw new ECFException(Messages.JMDNSDiscoveryContainer_DISCOVERY_NOT_INITIALIZED);
			final ServiceInfo svcInfo = createServiceInfoFromIServiceInfo(serviceInfo);
			checkServiceInfo(svcInfo);
			try {
				jmdns.registerService(svcInfo);
			} catch (final IOException e) {
				throw new ECFException(Messages.JMDNSDiscoveryContainer_EXCEPTION_REGISTER_SERVICE, e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#unregisterService(org.eclipse.ecf.discovery.IServiceInfo)
	 */
	public void unregisterService(IServiceInfo serviceInfo) {
		Assert.isNotNull(serviceInfo);
		synchronized (lock) {
			if (jmdns != null) {
				jmdns.unregisterService(createServiceInfoFromIServiceInfo(serviceInfo));
			}
		}
	}

	/**************************** JMDNS listeners ***********************************/

	private void runInThread(Runnable runnable) {
		SimpleFIFOQueue localQueue = null;
		synchronized (lock) {
			localQueue = queue;
		}
		if (localQueue != null)
			localQueue.enqueue(runnable);
	}

	/* (non-Javadoc)
	 * @see javax.jmdns.ServiceTypeListener#serviceTypeAdded(javax.jmdns.ServiceEvent)
	 */
	public void serviceTypeAdded(final ServiceEvent arg0) {
		Trace.trace(JMDNSPlugin.PLUGIN_ID, "serviceTypeAdded(" + arg0 + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		String st = arg0.getType();
		if (st == null)
			return;
		final IServiceTypeID serviceType = createServiceTypeID(st);
		runInThread(new Runnable() {
			public void run() {
				if (jmdns != null) {
					try {
						serviceTypes.add(serviceType);
						jmdns.addServiceListener(arg0.getType(), JMDNSDiscoveryContainer.this);
						fireTypeDiscovered(serviceType);
					} catch (final Exception e) {
						Trace.catching(JMDNSPlugin.PLUGIN_ID, JMDNSDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), "serviceTypeAdded", e); //$NON-NLS-1$
					}
				}
			}
		});

	}

	void fireTypeDiscovered(final IServiceTypeID serviceType) {
		fireServiceTypeDiscovered(new ServiceTypeContainerEvent(serviceType, getID()));
	}

	/* (non-Javadoc)
	 * @see javax.jmdns.ServiceListener#serviceAdded(javax.jmdns.ServiceEvent)
	 */
	public void serviceAdded(final ServiceEvent arg0) {
		Trace.trace(JMDNSPlugin.PLUGIN_ID, "serviceAdded(" + arg0 + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		runInThread(new Runnable() {
			public void run() {
				if (jmdns != null) {
					jmdns.requestServiceInfo(arg0.getType(), arg0.getName(), DEFAULT_REQUEST_TIMEOUT);
				}
			}
		});
	}

	/* (non-Javadoc)
	 * @see javax.jmdns.ServiceListener#serviceRemoved(javax.jmdns.ServiceEvent)
	 */
	public void serviceRemoved(final ServiceEvent arg0) {
		Trace.trace(JMDNSPlugin.PLUGIN_ID, "serviceRemoved(" + arg0 + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		runInThread(new Runnable() {
			public void run() {
				if (jmdns != null) {
					try {
						fireUndiscovered(createIServiceInfoFromServiceInfo(arg0.getInfo()));
					} catch (final Exception e) {
						Trace.catching(JMDNSPlugin.PLUGIN_ID, JMDNSDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), "serviceRemoved", e); //$NON-NLS-1$
					}
				}
			}
		});
	}

	void fireUndiscovered(IServiceInfo serviceInfo) {
		fireServiceUndiscovered(new ServiceContainerEvent(serviceInfo, getID()));
	}

	/* (non-Javadoc)
	 * @see javax.jmdns.ServiceListener#serviceResolved(javax.jmdns.ServiceEvent)
	 */
	public void serviceResolved(final ServiceEvent arg0) {
		Trace.trace(JMDNSPlugin.PLUGIN_ID, "serviceResolved(" + arg0 + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		runInThread(new Runnable() {
			public void run() {
				if (jmdns != null) {
					try {
						fireDiscovered(createIServiceInfoFromServiceInfo(arg0.getInfo()));
					} catch (final Exception e) {
						Trace.catching(JMDNSPlugin.PLUGIN_ID, JMDNSDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), "serviceResolved", e); //$NON-NLS-1$
					}
				}
			}
		});
	}

	void fireDiscovered(IServiceInfo serviceInfo) {
		fireServiceDiscovered(new ServiceContainerEvent(serviceInfo, getID()));
	}

	/*******************************************/
	private void checkServiceInfo(ServiceInfo serviceInfo) throws ECFException {
		final String serviceName = serviceInfo.getName();
		if (serviceName == null)
			throw new ECFException(Messages.JMDNSDiscoveryContainer_SERVICE_NAME_NOT_NULL);
	}

	private IServiceTypeID createServiceTypeID(String type) {
		IServiceID serviceID = createServiceID(type, null);
		if (serviceID == null)
			return null;
		return serviceID.getServiceTypeID();
	}

	IServiceInfo createIServiceInfoFromServiceInfo(final ServiceInfo serviceInfo) throws Exception {
		if (serviceInfo == null)
			return null;
		String st = serviceInfo.getType();
		String n = serviceInfo.getName();
		if (st == null || n == null)
			throw new InvalidObjectException(Messages.JMDNSDiscoveryContainer_EXCEPTION_SERVICEINFO_INVALID);
		final ServiceID sID = createServiceID(serviceInfo.getType(), serviceInfo.getName());
		if (sID == null)
			throw new InvalidObjectException(Messages.JMDNSDiscoveryContainer_EXCEPTION_SERVICEINFO_INVALID);
		final InetAddress addr = serviceInfo.getAddress();
		final int port = serviceInfo.getPort();
		final int priority = serviceInfo.getPriority();
		final int weight = serviceInfo.getWeight();
		final Properties props = new Properties();
		String uriProtocol = null;
		for (final Enumeration e = serviceInfo.getPropertyNames(); e.hasMoreElements();) {
			final String name = (String) e.nextElement();
			if (name.equals(SCHEME_PROPERTY))
				uriProtocol = serviceInfo.getPropertyString(name);
			else {
				Object value = serviceInfo.getPropertyString(name);
				if (value == null)
					value = serviceInfo.getPropertyBytes(name);
				if (value != null)
					props.put(name, value);
			}
		}
		return new org.eclipse.ecf.discovery.ServiceInfo(uriProtocol, addr.getHostAddress(), port, sID, priority, weight, new ServiceProperties(props));
	}

	private ServiceID createServiceID(String type, String name) {
		ServiceID id = null;
		try {
			id = (ServiceID) ServiceIDFactory.getDefault().createServiceID(getServicesNamespace(), type, name);
		} catch (final IDCreateException e) {
			// Should never happen
			Trace.catching(JMDNSPlugin.PLUGIN_ID, JMDNSDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), "createServiceID", e); //$NON-NLS-1$
		}
		return id;
	}

	private ServiceInfo createServiceInfoFromIServiceInfo(IServiceInfo serviceInfo) {
		if (serviceInfo == null)
			return null;
		final IServiceID sID = serviceInfo.getServiceID();
		final Hashtable props = new Hashtable();
		final IServiceProperties svcProps = serviceInfo.getServiceProperties();
		if (svcProps != null) {
			for (final Enumeration e = svcProps.getPropertyNames(); e.hasMoreElements();) {
				final String key = (String) e.nextElement();
				final Object val = svcProps.getProperty(key);
				if (val != null) {
					props.put(key, val);
				}
			}
		}
		// Add URI scheme to props
		URI location = serviceInfo.getLocation();
		props.put(SCHEME_PROPERTY, location.getScheme());
		int priority = (serviceInfo.getPriority() == -1) ? 0 : serviceInfo.getPriority();
		int weight = (serviceInfo.getWeight() == -1) ? 0 : serviceInfo.getWeight();
		final ServiceInfo si = new ServiceInfo(sID.getServiceTypeID().getInternal(), sID.getServiceName(), location.getPort(), priority, weight, props);
		return si;
	}

}
