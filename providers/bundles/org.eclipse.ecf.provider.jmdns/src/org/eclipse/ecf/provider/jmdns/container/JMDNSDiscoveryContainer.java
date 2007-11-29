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
import java.net.InetAddress;
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
	public static final int DEFAULT_REQUEST_TIMEOUT = 3000;

	private static int instanceCount = 0;

	private InetAddress intf = null;
	JmDNS jmdns = null;
	private ID targetID = null;

	List serviceTypes = null;

	boolean disposed = false;
	Object lock = new Object();

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
		new Thread(runnable).start();
	}

	/* (non-Javadoc)
	 * @see javax.jmdns.ServiceTypeListener#serviceTypeAdded(javax.jmdns.ServiceEvent)
	 */
	public void serviceTypeAdded(final ServiceEvent arg0) {
		Trace.trace(JMDNSPlugin.PLUGIN_ID, "serviceTypeAdded(" + arg0 + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		final IServiceTypeID serviceType = createServiceTypeID(arg0.getType());
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
		return createServiceID(type, null).getServiceTypeID();
	}

	IServiceInfo createIServiceInfoFromServiceInfo(final ServiceInfo serviceInfo) throws Exception {
		if (serviceInfo == null)
			return null;
		final ServiceID sID = createServiceID(serviceInfo.getType(), serviceInfo.getName());
		final InetAddress addr = serviceInfo.getAddress();
		final int port = serviceInfo.getPort();
		final int priority = serviceInfo.getPriority();
		final int weight = serviceInfo.getWeight();
		final Properties props = new Properties();
		for (final Enumeration e = serviceInfo.getPropertyNames(); e.hasMoreElements();) {
			final String name = (String) e.nextElement();
			Object value = serviceInfo.getPropertyString(name);
			if (value == null)
				value = serviceInfo.getPropertyBytes(name);
			if (value != null)
				props.put(name, value);
		}
		final ServiceProperties svcProperties = new ServiceProperties(props);
		final IServiceInfo newInfo = new JMDNSServiceInfo(addr, sID, port, priority, weight, svcProperties);
		return newInfo;
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
		final ServiceInfo si = new ServiceInfo(sID.getServiceTypeID().getInternal(), sID.getServiceName(), serviceInfo.getLocation().getPort(), serviceInfo.getPriority(), serviceInfo.getWeight(), props);
		return si;
	}

}
