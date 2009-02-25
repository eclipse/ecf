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

import java.io.*;
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
import org.eclipse.ecf.core.util.ECFRuntimeException;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.discovery.*;
import org.eclipse.ecf.discovery.identity.*;
import org.eclipse.ecf.discovery.service.IDiscoveryService;
import org.eclipse.ecf.internal.provider.jmdns.*;
import org.eclipse.ecf.provider.jmdns.identity.JMDNSNamespace;

public class JMDNSDiscoveryContainer extends AbstractDiscoveryContainerAdapter implements IDiscoveryService, ServiceListener, ServiceTypeListener {

	private static final String SCHEME_PROPERTY = "jmdns.ptcl"; //$NON-NLS-1$
	private static final String URI_PATH_PROPERTY = "jmdns.uripath"; //$NON-NLS-1$
	private static final String NAMING_AUTHORITY_PROPERTY = "jmdns.namingauthority"; //$NON-NLS-1$

	public static final int DEFAULT_REQUEST_TIMEOUT = 3000;

	private static int instanceCount = 0;

	private InetAddress intf = null;
	JmDNS jmdns = null;
	private ID targetID = null;

	List serviceTypes = null;

	boolean disposed = false;
	final Object lock = new Object();

	SimpleFIFOQueue queue = null;
	Thread notificationThread = null;

	public JMDNSDiscoveryContainer(final InetAddress addr) {
		super(JMDNSNamespace.NAME, new DiscoveryContainerConfig(IDFactory.getDefault().createStringID(JMDNSDiscoveryContainer.class.getName() + ";" + addr.toString() + ";" + instanceCount++))); //$NON-NLS-1$  //$NON-NLS-2$
		Assert.isNotNull(addr);
		intf = addr;
		serviceTypes = new ArrayList();
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
	public void connect(final ID targetID1, final IConnectContext joinContext) throws ContainerConnectException {
		synchronized (lock) {
			if (disposed)
				throw new ContainerConnectException(Messages.JMDNSDiscoveryContainer_EXCEPTION_CONTAINER_DISPOSED);
			if (this.targetID != null)
				throw new ContainerConnectException(Messages.JMDNSDiscoveryContainer_EXCEPTION_ALREADY_CONNECTED);
			this.targetID = (targetID1 == null) ? getConfig().getID() : targetID1;
			fireContainerEvent(new ContainerConnectingEvent(this.getID(), this.targetID, joinContext));
			initializeQueue();
			try {
				this.jmdns = JmDNS.create(intf);
				jmdns.addServiceTypeListener(this);
			} catch (final IOException e) {
				if (this.jmdns != null) {
					jmdns.close();
					jmdns = null;
				}
				throw new ContainerConnectException(Messages.JMDNSDiscoveryContainer_EXCEPTION_CREATE_JMDNS_INSTANCE, e);
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
					final Runnable runnable = (Runnable) queue.dequeue();
					if (Thread.currentThread().isInterrupted() || runnable == null)
						break;
					try {
						runnable.run();
					} catch (final Throwable t) {
						handleRuntimeException(t);
					}
				}
			}
		}, "JMDNS Discovery Thread"); //$NON-NLS-1$
		notificationThread.start();
	}

	protected void handleRuntimeException(final Throwable t) {
		// Nothing to do except log
		JMDNSPlugin.getDefault().logException("handleRuntimeException", t); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#disconnect()
	 */
	public void disconnect() {
		synchronized (lock) {
			if (getConnectedID() == null || disposed) {
				return;
			}
			final ID connectedID = getConnectedID();
			fireContainerEvent(new ContainerDisconnectingEvent(this.getID(), connectedID));
			queue.close();
			notificationThread = null;
			this.targetID = null;
			serviceTypes.clear();
			jmdns.close();
			jmdns = null;
			fireContainerEvent(new ContainerDisconnectedEvent(this.getID(), connectedID));
		}
	}

	/************************* IDiscoveryContainerAdapter methods *********************/

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServiceInfo(org.eclipse.ecf.discovery.identity.IServiceID)
	 */
	public IServiceInfo getServiceInfo(final IServiceID service) {
		Assert.isNotNull(service);
		synchronized (lock) {
			try {
				final ServiceInfo serviceInfo = jmdns.getServiceInfo(service.getServiceTypeID().getInternal(), service.getServiceName());
				return (serviceInfo == null) ? null : createIServiceInfoFromServiceInfo(serviceInfo);
			} catch (final Exception e) {
				Trace.catching(JMDNSPlugin.PLUGIN_ID, JMDNSDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), "getServiceInfo", e); //$NON-NLS-1$
				return null;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServices()
	 */
	public IServiceInfo[] getServices() {
		synchronized (lock) {
			final IServiceTypeID[] serviceTypeArray = getServiceTypes();
			final List results = new ArrayList();
			for (int i = 0; i < serviceTypeArray.length; i++) {
				final IServiceTypeID stid = serviceTypeArray[i];
				if (stid != null)
					results.addAll(Arrays.asList(getServices(stid)));
			}
			return (IServiceInfo[]) results.toArray(new IServiceInfo[] {});
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServices(org.eclipse.ecf.discovery.identity.IServiceTypeID)
	 */
	public IServiceInfo[] getServices(final IServiceTypeID type) {
		Assert.isNotNull(type);
		final List serviceInfos = new ArrayList();
		synchronized (lock) {
			// We don't know the naming authority yet (it's part of the service properties)
			for (final Iterator itr = serviceTypes.iterator(); itr.hasNext();) {
				final IServiceTypeID serviceType = (IServiceTypeID) itr.next();
				if (Arrays.equals(serviceType.getServices(), type.getServices()) && Arrays.equals(serviceType.getProtocols(), type.getProtocols()) && Arrays.equals(serviceType.getScopes(), type.getScopes())) {
					final ServiceInfo[] infos = jmdns.list(type.getInternal());
					for (int i = 0; i < infos.length; i++) {
						try {
							if (infos[i] != null) {
								final IServiceInfo si = createIServiceInfoFromServiceInfo(infos[i]);
								if (si != null)
									serviceInfos.add(si);
							}
						} catch (final Exception e) {
							Trace.catching(JMDNSPlugin.PLUGIN_ID, JMDNSDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), "getServices", e); //$NON-NLS-1$
						}
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
		synchronized (lock) {
			return (IServiceTypeID[]) serviceTypes.toArray(new IServiceTypeID[] {});
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#registerService(org.eclipse.ecf.discovery.IServiceInfo)
	 */
	public void registerService(final IServiceInfo serviceInfo) {
		Assert.isNotNull(serviceInfo);
		final ServiceInfo svcInfo = createServiceInfoFromIServiceInfo(serviceInfo);
		checkServiceInfo(svcInfo);
		try {
			jmdns.registerService(svcInfo);
		} catch (final IOException e) {
			throw new ECFRuntimeException(Messages.JMDNSDiscoveryContainer_EXCEPTION_REGISTER_SERVICE, e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#unregisterService(org.eclipse.ecf.discovery.IServiceInfo)
	 */
	public void unregisterService(final IServiceInfo serviceInfo) {
		Assert.isNotNull(serviceInfo);
		final ServiceInfo si = createServiceInfoFromIServiceInfo(serviceInfo);
		jmdns.unregisterService(si);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.AbstractDiscoveryContainerAdapter#purgeCache()
	 */
	public IServiceInfo[] purgeCache() {
		synchronized (lock) {
			serviceTypes.clear();
		}
		return super.purgeCache();
	}

	/**************************** JMDNS listeners ***********************************/

	private void runInThread(final Runnable runnable) {
		queue.enqueue(runnable);
	}

	/* (non-Javadoc)
	 * @see javax.jmdns.ServiceTypeListener#serviceTypeAdded(javax.jmdns.ServiceEvent)
	 */
	public void serviceTypeAdded(final ServiceEvent arg0) {
		arg0.getDNS().addServiceListener(arg0.getType(), JMDNSDiscoveryContainer.this);
	}

	void fireTypeDiscovered(final IServiceTypeID serviceType) {
		fireServiceTypeDiscovered(new ServiceTypeContainerEvent(serviceType, getID()));
	}

	/* (non-Javadoc)
	 * @see javax.jmdns.ServiceListener#serviceAdded(javax.jmdns.ServiceEvent)
	 */
	public void serviceAdded(final ServiceEvent arg0) {
		Trace.trace(JMDNSPlugin.PLUGIN_ID, "serviceAdded(" + arg0.getName() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		runInThread(new Runnable() {
			public void run() {
				final String serviceType = arg0.getType();
				final String serviceName = arg0.getName();
				IServiceInfo aServiceInfo = null;
				synchronized (lock) {
					if (getConnectedID() == null || disposed) {
						return;
					}

					// explicitly get the service to determine the naming authority (part of the service properties)
					final ServiceInfo info = jmdns.getServiceInfo(serviceType, serviceName);
					try {
						aServiceInfo = createIServiceInfoFromServiceInfo(info);
						serviceTypes.add(aServiceInfo.getServiceID().getServiceTypeID());
					} catch (final Exception e) {
						return;
					}
				}
				fireTypeDiscovered(aServiceInfo.getServiceID().getServiceTypeID());
				fireDiscovered(aServiceInfo);
			}
		});
	}

	/* (non-Javadoc)
	 * @see javax.jmdns.ServiceListener#serviceRemoved(javax.jmdns.ServiceEvent)
	 */
	public void serviceRemoved(final ServiceEvent arg0) {
		Trace.trace(JMDNSPlugin.PLUGIN_ID, "serviceRemoved(" + arg0.getName() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		runInThread(new Runnable() {
			public void run() {
				if (getConnectedID() == null || disposed) {
					return;
				}
				try {
					fireUndiscovered(createIServiceInfoFromServiceEvent(arg0));
				} catch (final Exception e) {
					Trace.catching(JMDNSPlugin.PLUGIN_ID, JMDNSDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), "serviceRemoved", e); //$NON-NLS-1$
				}
			}
		});
	}

	void fireUndiscovered(final IServiceInfo serviceInfo) {
		fireServiceUndiscovered(new ServiceContainerEvent(serviceInfo, getID()));
	}

	/* (non-Javadoc)
	 * @see javax.jmdns.ServiceListener#serviceResolved(javax.jmdns.ServiceEvent)
	 */
	public void serviceResolved(final ServiceEvent arg0) {
		Trace.trace(JMDNSPlugin.PLUGIN_ID, "serviceResolved(" + arg0.getName() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	void fireDiscovered(final IServiceInfo serviceInfo) {
		fireServiceDiscovered(new ServiceContainerEvent(serviceInfo, getID()));
	}

	/*******************************************/
	private void checkServiceInfo(final ServiceInfo serviceInfo) {
		final String serviceName = serviceInfo.getName();
		if (serviceName == null)
			throw new ECFRuntimeException(Messages.JMDNSDiscoveryContainer_SERVICE_NAME_NOT_NULL);
	}

	IServiceInfo createIServiceInfoFromServiceEvent(final ServiceEvent event) throws Exception {
		final ServiceInfo si = event.getInfo();
		if (si != null)
			return createIServiceInfoFromServiceInfo(si);
		// else service info from JMDNS is null...and we need to create IServiceInfo ourselves
		final ServiceID sID = createServiceID(event.getType(), event.getName());
		if (sID == null)
			throw new InvalidObjectException(Messages.JMDNSDiscoveryContainer_EXCEPTION_SERVICEINFO_INVALID);
		return new org.eclipse.ecf.discovery.ServiceInfo(null, null, -1, sID);
	}

	IServiceInfo createIServiceInfoFromServiceInfo(final ServiceInfo serviceInfo) throws Exception {
		Assert.isNotNull(serviceInfo);
		final String st = serviceInfo.getType();
		final String n = serviceInfo.getName();
		if (st == null || n == null)
			throw new InvalidObjectException(Messages.JMDNSDiscoveryContainer_EXCEPTION_SERVICEINFO_INVALID);
		final InetAddress addr = serviceInfo.getAddress();
		final int port = serviceInfo.getPort();
		final int priority = serviceInfo.getPriority();
		final int weight = serviceInfo.getWeight();
		final Properties props = new Properties();
		String uriProtocol = null;
		String uriPath = ""; //$NON-NLS-1$
		String namingAuthority = IServiceTypeID.DEFAULT_NA;
		for (final Enumeration e = serviceInfo.getPropertyNames(); e.hasMoreElements();) {
			final String key = (String) e.nextElement();
			if (SCHEME_PROPERTY.equals(key)) {
				uriProtocol = serviceInfo.getPropertyString(key);
			} else if (URI_PATH_PROPERTY.equals(key)) {
				uriPath = serviceInfo.getPropertyString(key);
			} else if (NAMING_AUTHORITY_PROPERTY.equals(key)) {
				namingAuthority = serviceInfo.getPropertyString(key);
			} else {
				final byte[] bytes = serviceInfo.getPropertyBytes(key);
				try {
					final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
					final Object object = in.readObject();
					in.close();
					props.put(key, object);
				} catch (final StreamCorruptedException ioe) {
					props.put(key, serviceInfo.getPropertyString(key));
				}
			}
		}
		final URI uri = URI.create(((uriProtocol == null) ? "unknown" : uriProtocol) + "://" + addr.getHostAddress() + ":" + port + ((uriPath == null) ? "" : uriPath)); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		final ServiceID sID = createServiceID(serviceInfo.getType() + "_" + namingAuthority, serviceInfo.getName()); //$NON-NLS-1$
		if (sID == null) {
			throw new InvalidObjectException(Messages.JMDNSDiscoveryContainer_EXCEPTION_SERVICEINFO_INVALID);
		}
		return new org.eclipse.ecf.discovery.ServiceInfo(uri, sID, priority, weight, new ServiceProperties(props));
	}

	ServiceID createServiceID(final String type, final String name) {
		ServiceID id = null;
		try {
			id = (ServiceID) ServiceIDFactory.getDefault().createServiceID(getServicesNamespace(), type, name);
		} catch (final IDCreateException e) {
			// Should never happen
			Trace.catching(JMDNSPlugin.PLUGIN_ID, JMDNSDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), "createServiceID", e); //$NON-NLS-1$
		}
		return id;
	}

	private ServiceInfo createServiceInfoFromIServiceInfo(final IServiceInfo serviceInfo) {
		if (serviceInfo == null)
			return null;
		final IServiceID sID = serviceInfo.getServiceID();
		final Hashtable props = new Hashtable();
		final IServiceProperties svcProps = serviceInfo.getServiceProperties();
		if (svcProps != null) {
			for (final Enumeration e = svcProps.getPropertyNames(); e.hasMoreElements();) {
				final String key = (String) e.nextElement();
				final Object val = svcProps.getProperty(key);
				if (val instanceof String) {
					props.put(key, val);
				} else if (val instanceof Serializable) {
					final ByteArrayOutputStream bos = new ByteArrayOutputStream();
					try {
						final ObjectOutputStream out = new ObjectOutputStream(bos);
						out.writeObject(val);
						out.close();
					} catch (final IOException e1) {
						e1.printStackTrace();
					}
					final byte[] buf = bos.toByteArray();
					props.put(key, buf);
					//				} else if (svcProps.getPropertyBytes(key) != null) {
					//					byte[] bytes = svcProps.getPropertyBytes(key);
					//					props.put(key, bytes);
				} else if (val != null) {
					props.put(key, val.toString());
				}
			}
		}
		// Add URI scheme to props
		final URI location = serviceInfo.getLocation();
		if (location != null) {
			props.put(SCHEME_PROPERTY, location.getScheme());
			props.put(URI_PATH_PROPERTY, location.getPath());
		}
		props.put(NAMING_AUTHORITY_PROPERTY, serviceInfo.getServiceID().getServiceTypeID().getNamingAuthority());
		final String serviceName = sID.getServiceName() == null ? location.getHost() : sID.getServiceName();
		final ServiceInfo si = ServiceInfo.create(sID.getServiceTypeID().getInternal(), serviceName, location.getPort(), serviceInfo.getWeight(), serviceInfo.getPriority(), props);
		return si;
	}

}
