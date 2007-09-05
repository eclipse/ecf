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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.jmdns.ServiceTypeListener;

import org.eclipse.ecf.core.AbstractContainer;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.events.ContainerConnectedEvent;
import org.eclipse.ecf.core.events.ContainerConnectingEvent;
import org.eclipse.ecf.core.events.ContainerDisconnectedEvent;
import org.eclipse.ecf.core.events.ContainerDisconnectingEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.IServiceListener;
import org.eclipse.ecf.discovery.IServiceProperties;
import org.eclipse.ecf.discovery.IServiceTypeListener;
import org.eclipse.ecf.discovery.ServiceContainerEvent;
import org.eclipse.ecf.discovery.ServiceProperties;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.discovery.identity.ServiceID;
import org.eclipse.ecf.discovery.service.IDiscoveryService;
import org.eclipse.ecf.internal.provider.jmdns.JMDNSDebugOptions;
import org.eclipse.ecf.internal.provider.jmdns.JMDNSPlugin;
import org.eclipse.ecf.internal.provider.jmdns.Messages;
import org.eclipse.ecf.provider.jmdns.identity.JMDNSNamespace;
import org.eclipse.ecf.provider.jmdns.identity.JMDNSServiceID;

public class JMDNSDiscoveryContainer extends AbstractContainer implements IContainer, IDiscoveryService, ServiceListener, ServiceTypeListener {
	public static final int DEFAULT_REQUEST_TIMEOUT = 3000;

	private static int instanceCount = 0;

	private ID localID = null;
	private InetAddress intf = null;
	private JmDNS jmdns = null;
	private final Map serviceListeners = new HashMap();
	private final List serviceTypeListeners = new ArrayList();

	public JMDNSDiscoveryContainer() throws IOException, IDCreateException {
		this(null);
	}

	public ID getID() {
		return localID;
	}

	public JMDNSDiscoveryContainer(InetAddress addr) throws IOException, IDCreateException {
		super();
		intf = (addr == null) ? InetAddress.getLocalHost() : addr;
		this.localID = IDFactory.getDefault().createStringID(intf.toString() + ";" + instanceCount++); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#addServiceListener(java.lang.String, org.eclipse.ecf.discovery.IServiceListener)
	 */
	public void addServiceListener(String type, IServiceListener listener) {
		if (type == null || listener == null)
			return;
		synchronized (serviceListeners) {
			List v = (List) serviceListeners.get(type);
			if (v == null) {
				v = new ArrayList();
				serviceListeners.put(type, v);
			}
			v.add(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#addServiceTypeListener(org.eclipse.ecf.discovery.IServiceTypeListener)
	 */
	public void addServiceTypeListener(IServiceTypeListener listener) {
		synchronized (serviceTypeListeners) {
			serviceTypeListeners.add(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#dispose()
	 */
	public void dispose() {
		disconnect();
		super.dispose();
	}

	protected void fireServiceAdded(ServiceEvent arg0) {
		final IServiceInfo iinfo = createIServiceInfoFromServiceEvent(arg0);
		List notify = null;
		synchronized (serviceListeners) {
			final List orig = (List) serviceListeners.get(arg0.getType());
			if (orig != null)
				notify = new ArrayList(orig);
		}
		if (notify != null) {
			for (final Iterator i = notify.iterator(); i.hasNext();) {
				final IServiceListener l = (IServiceListener) i.next();
				l.serviceAdded(new ServiceContainerEvent(iinfo, getID()));
			}
		}
	}

	protected void fireServiceRemoved(ServiceEvent arg0) {
		final IServiceInfo iinfo = createIServiceInfoFromServiceEvent(arg0);
		List notify = null;
		synchronized (serviceListeners) {
			final List orig = (List) serviceListeners.get(arg0.getType());
			if (orig != null)
				notify = new ArrayList(orig);
		}
		if (notify != null) {
			for (final Iterator i = notify.iterator(); i.hasNext();) {
				final IServiceListener l = (IServiceListener) i.next();
				l.serviceRemoved(new ServiceContainerEvent(iinfo, getID()));
			}
		}
	}

	protected void fireServiceResolved(ServiceEvent arg0) {
		final IServiceInfo iinfo = createIServiceInfoFromServiceEvent(arg0);
		List notify = null;
		synchronized (serviceListeners) {
			final List orig = (List) serviceListeners.get(arg0.getType());
			if (orig != null)
				notify = new ArrayList(orig);
		}
		if (notify != null) {
			for (final Iterator i = notify.iterator(); i.hasNext();) {
				final IServiceListener l = (IServiceListener) i.next();
				l.serviceResolved(new ServiceContainerEvent(iinfo, getID()));
			}
		}
	}

	protected void fireServiceTypeAdded(ServiceEvent arg0) {
		List notify = null;
		synchronized (serviceTypeListeners) {
			notify = new ArrayList(serviceTypeListeners);
		}
		for (final Iterator i = notify.iterator(); i.hasNext();) {
			final IServiceTypeListener l = (IServiceTypeListener) i.next();
			l.serviceTypeAdded(new ServiceContainerEvent(createIServiceInfoFromServiceEvent(arg0), getID()));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#getConnectedID()
	 */
	public ID getConnectedID() {
		return getID();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServiceInfo(org.eclipse.ecf.discovery.identity.IServiceID, int)
	 */
	public IServiceInfo getServiceInfo(IServiceID service, int timeout) {
		Trace.entering(JMDNSPlugin.PLUGIN_ID, JMDNSDebugOptions.METHODS_ENTERING, this.getClass(), "getServiceInfo", new Object[] {service, new Integer(timeout)}); //$NON-NLS-1$
		IServiceInfo result = null;
		if (jmdns != null)
			result = createIServiceInfoFromServiceInfo(jmdns.getServiceInfo(service.getServiceTypeID().getName(), service.getServiceName(), timeout));
		Trace.exiting(JMDNSPlugin.PLUGIN_ID, JMDNSDebugOptions.METHODS_ENTERING, this.getClass(), "getServiceInfo", result); //$NON-NLS-1$
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServices(java.lang.String)
	 */
	public IServiceInfo[] getServices(String type) {
		IServiceInfo svs[] = new IServiceInfo[0];
		if (jmdns != null) {
			final ServiceInfo[] svcs = jmdns.list(type);
			if (svcs != null) {
				svs = new IServiceInfo[svcs.length];
				for (int i = 0; i < svcs.length; i++) {
					svs[i] = createIServiceInfoFromServiceInfo(svcs[i]);
				}
			}
		}
		return svs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#connect(org.eclipse.ecf.core.identity.ID, org.eclipse.ecf.core.security.IConnectContext)
	 */
	public void connect(ID groupID, IConnectContext joinContext) throws ContainerConnectException {
		if (this.jmdns != null)
			throw new ContainerConnectException(Messages.JMDNSDiscoveryContainer_EXCEPTION_ALREADY_CONNECTED);
		fireContainerEvent(new ContainerConnectingEvent(this.getID(), groupID, joinContext));
		try {
			this.jmdns = new JmDNS(intf);
			jmdns.addServiceTypeListener(this);
			if (groupID != null && groupID instanceof JMDNSServiceID) {
				final ServiceID svcid = (ServiceID) groupID;
				jmdns.addServiceListener(svcid.getServiceTypeID().getName(), this);
			}
		} catch (final IOException e) {
			final ContainerConnectException soe = new ContainerConnectException(Messages.JMDNSDiscoveryContainer_EXCEPTION_CREATE_JMDNS_INSTANCE);
			throw soe;
		}
		fireContainerEvent(new ContainerConnectedEvent(this.getID(), groupID));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#disconnect()
	 */
	public void disconnect() {
		fireContainerEvent(new ContainerDisconnectingEvent(this.getID(), getConnectedID()));
		if (this.jmdns != null) {
			jmdns.close();
			jmdns = null;
		}
		fireContainerEvent(new ContainerDisconnectedEvent(this.getID(), getConnectedID()));
	}

	protected IServiceInfo createIServiceInfoFromServiceEvent(ServiceEvent event) {
		final ServiceID sID = createServiceID(event.getType(), event.getName());
		final ServiceInfo sinfo = event.getInfo();
		if (sinfo != null) {
			return createIServiceInfoFromServiceInfo(sinfo);
		}
		final IServiceInfo newInfo = new JMDNSServiceInfo(null, sID, -1, -1, -1, new ServiceProperties());
		return newInfo;
	}

	protected IServiceInfo createIServiceInfoFromServiceInfo(final ServiceInfo serviceInfo) {
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
		final IServiceProperties newProps = new IServiceProperties() {
			public Enumeration getPropertyNames() {
				return svcProperties.getPropertyNames();
			}

			public String getPropertyString(String name) {
				return svcProperties.getPropertyString(name);
			}

			public byte[] getPropertyBytes(String name) {
				return svcProperties.getPropertyBytes(name);
			}

			public Object getProperty(String name) {
				return svcProperties.getProperty(name);
			}

			public Object setProperty(String name, Object value) {
				return svcProperties.setProperty(name, value);
			}

			public Object setPropertyBytes(String name, byte[] value) {
				return svcProperties.setPropertyBytes(name, value);
			}

			public Object setPropertyString(String name, String value) {
				return svcProperties.setPropertyString(name, value);
			}
		};
		final IServiceInfo newInfo = new JMDNSServiceInfo(addr, sID, port, priority, weight, newProps);
		return newInfo;
	}

	protected ServiceID createServiceID(String type, String name) {
		ServiceID id = null;
		try {
			id = (ServiceID) IDFactory.getDefault().createID(JMDNSNamespace.NAME, new Object[] {type, name});
		} catch (final IDCreateException e) {
			// Should never happen
			Trace.catching(JMDNSPlugin.PLUGIN_ID, JMDNSDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), "createServiceID", e); //$NON-NLS-1$
		}
		return id;
	}

	protected ServiceInfo createServiceInfoFromIServiceInfo(IServiceInfo serviceInfo) {
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
		final ServiceInfo si = new ServiceInfo(sID.getServiceTypeID().getName(), sID.getServiceName(), serviceInfo.getPort(), serviceInfo.getPriority(), serviceInfo.getWeight(), props);
		return si;
	}

	protected String prepareSvcTypeForBonjour(String svcType) {
		String result = svcType;
		if (svcType.endsWith(Messages.JMDNSDiscoveryContainer_JMDNS_LOCAL_SUFFIX)) {
			result = svcType.substring(0, svcType.indexOf(Messages.JMDNSDiscoveryContainer_JMDNS_LOCAL_SUFFIX));
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#registerService(org.eclipse.ecf.discovery.IServiceInfo)
	 */
	public void registerService(IServiceInfo serviceInfo) throws ECFException {
		try {
			registerServiceWithJmDNS(serviceInfo);
		} catch (final IOException e) {
			throw new ECFException(Messages.JMDNSDiscoveryContainer_EXCEPTION_REGISTER_SERVICE, e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#registerServiceType(java.lang.String)
	 */
	public void registerServiceType(String serviceType) {
		if (jmdns != null) {
			jmdns.registerServiceType(serviceType);
			jmdns.addServiceListener(serviceType, this);
		}
	}

	protected void registerServiceWithJmDNS(IServiceInfo serviceInfo) throws IOException {
		if (jmdns != null) {
			jmdns.registerService(createServiceInfoFromIServiceInfo(serviceInfo));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#removeServiceListener(java.lang.String, org.eclipse.ecf.discovery.IServiceListener)
	 */
	public void removeServiceListener(String type, IServiceListener listener) {
		if (type == null || listener == null)
			return;
		synchronized (serviceListeners) {
			final List v = (List) serviceListeners.get(type);
			if (v == null) {
				return;
			}
			v.remove(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#removeServiceTypeListener(org.eclipse.ecf.discovery.IServiceTypeListener)
	 */
	public void removeServiceTypeListener(IServiceTypeListener listener) {
		synchronized (serviceTypeListeners) {
			serviceTypeListeners.add(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#requestServiceInfo(org.eclipse.ecf.discovery.identity.IServiceID, int)
	 */
	public void requestServiceInfo(IServiceID service, int timeout) {
		if (jmdns != null) {
			jmdns.requestServiceInfo(service.getServiceTypeID().getName(), service.getServiceName(), timeout);
		}
	}

	public void serviceAdded(ServiceEvent arg0) {
		if (jmdns != null) {
			try {
				fireServiceAdded(arg0);
			} catch (final Exception e) {
				Trace.catching(JMDNSPlugin.PLUGIN_ID, JMDNSDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), "serviceAdded", e); //$NON-NLS-1$
			}
		}
	}

	public void serviceRemoved(ServiceEvent arg0) {
		if (jmdns != null) {
			try {
				fireServiceRemoved(arg0);
			} catch (final Exception e) {
				Trace.catching(JMDNSPlugin.PLUGIN_ID, JMDNSDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), "serviceRemoved", e); //$NON-NLS-1$
			}
		}
	}

	public void serviceResolved(ServiceEvent arg0) {
		if (jmdns != null) {
			try {
				fireServiceResolved(arg0);
			} catch (final Exception e) {
				Trace.catching(JMDNSPlugin.PLUGIN_ID, JMDNSDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), "serviceResolved", e); //$NON-NLS-1$
			}
		}
	}

	public void serviceTypeAdded(ServiceEvent arg0) {
		if (jmdns != null) {
			try {
				fireServiceTypeAdded(arg0);
			} catch (final Exception e) {
				Trace.catching(JMDNSPlugin.PLUGIN_ID, JMDNSDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), "serviceResolved", e); //$NON-NLS-1$
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#unregisterService(org.eclipse.ecf.discovery.IServiceInfo)
	 */
	public void unregisterService(IServiceInfo serviceInfo) {
		if (jmdns != null) {
			jmdns.unregisterService(createServiceInfoFromIServiceInfo(serviceInfo));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#getConnectNamespace()
	 */
	public Namespace getConnectNamespace() {
		return IDFactory.getDefault().getNamespaceByName(JMDNSNamespace.NAME);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#addServiceListener(org.eclipse.ecf.discovery.identity.IServiceTypeID, org.eclipse.ecf.discovery.IServiceListener)
	 */
	public void addServiceListener(IServiceTypeID type, IServiceListener listener) {
		addServiceListener(type.getName(), listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServices(org.eclipse.ecf.discovery.identity.IServiceTypeID)
	 */
	public IServiceInfo[] getServices(IServiceTypeID type) {
		return getServices(type.getName());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#registerServiceType(org.eclipse.ecf.discovery.identity.IServiceTypeID)
	 */
	public void registerServiceType(IServiceTypeID type) {
		registerServiceType(type.getName());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#removeServiceListener(org.eclipse.ecf.discovery.identity.IServiceTypeID, org.eclipse.ecf.discovery.IServiceListener)
	 */
	public void removeServiceListener(IServiceTypeID type, IServiceListener listener) {
		removeServiceListener(type.getName(), listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServicesNamespace()
	 */
	public Namespace getServicesNamespace() {
		return IDFactory.getDefault().getNamespaceByName(JMDNSNamespace.NAME);
	}

}
