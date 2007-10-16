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
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.events.*;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.discovery.*;
import org.eclipse.ecf.discovery.identity.*;
import org.eclipse.ecf.discovery.service.IDiscoveryService;
import org.eclipse.ecf.internal.provider.jmdns.*;
import org.eclipse.ecf.provider.jmdns.identity.*;

public class JMDNSDiscoveryContainer extends AbstractDiscoveryContainerAdapter implements IContainer, IDiscoveryService, ServiceListener, ServiceTypeListener {
	public static final int DEFAULT_REQUEST_TIMEOUT = 3000;

	private static int instanceCount = 0;

	private InetAddress intf = null;
	private JmDNS jmdns = null;
	private ID targetID = null;

	public JMDNSDiscoveryContainer(InetAddress addr) throws IDCreateException {
		super(JMDNSNamespace.NAME, new DiscoveryContainerConfig(IDFactory.getDefault().createStringID(JMDNSDiscoveryContainer.class.getName() + ";" + addr.toString() + ";" + instanceCount++))); //$NON-NLS-1$  //$NON-NLS-2$
		intf = addr;
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

	/**
	 * @deprecated
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
	public void connect(ID targetID1, IConnectContext joinContext) throws ContainerConnectException {
		if (this.targetID != null)
			throw new ContainerConnectException(Messages.JMDNSDiscoveryContainer_EXCEPTION_ALREADY_CONNECTED);
		this.targetID = (targetID1 == null) ? getConfig().getID() : targetID1;
		fireContainerEvent(new ContainerConnectingEvent(this.getID(), this.targetID, joinContext));
		try {
			this.jmdns = new JmDNS(intf);
			jmdns.addServiceTypeListener(this);
			if (targetID1 != null && targetID1 instanceof JMDNSServiceID) {
				final JMDNSServiceID svcid = (JMDNSServiceID) targetID1;
				final JMDNSServiceTypeID serviceTypeID = (JMDNSServiceTypeID) svcid.getServiceTypeID();
				jmdns.addServiceListener(serviceTypeID.getInternal(), this);
			}
		} catch (final IOException e) {
			throw new ContainerConnectException(Messages.JMDNSDiscoveryContainer_EXCEPTION_CREATE_JMDNS_INSTANCE);
		}
		fireContainerEvent(new ContainerConnectedEvent(this.getID(), this.targetID));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#disconnect()
	 */
	public void disconnect() {
		final ID connectedID = getConnectedID();
		fireContainerEvent(new ContainerDisconnectingEvent(this.getID(), connectedID));
		if (this.jmdns != null) {
			jmdns.close();
			jmdns = null;
		}
		this.targetID = null;
		clearListeners();
		fireContainerEvent(new ContainerDisconnectedEvent(this.getID(), connectedID));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#registerService(org.eclipse.ecf.discovery.IServiceInfo)
	 */
	public void registerService(IServiceInfo serviceInfo) throws ECFException {
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

	/**
	 * @deprecated
	 */
	public void registerServiceType(String serviceType) {
		if (jmdns != null && serviceType != null) {
			jmdns.registerServiceType(serviceType);
			jmdns.addServiceListener(serviceType, this);
		}
	}

	protected void checkServiceInfo(ServiceInfo serviceInfo) throws ECFException {
		final String serviceName = serviceInfo.getName();
		if (serviceName == null)
			throw new ECFException(Messages.JMDNSDiscoveryContainer_SERVICE_NAME_NOT_NULL);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#requestServiceInfo(org.eclipse.ecf.discovery.identity.IServiceID, int)
	 */
	public void requestServiceInfo(IServiceID service, int timeout) {
		if (jmdns != null) {
			if (service instanceof JMDNSServiceID) {
				final JMDNSServiceID svcID = (JMDNSServiceID) service;
				final JMDNSServiceTypeID typeID = (JMDNSServiceTypeID) svcID.getServiceTypeID();
				jmdns.requestServiceInfo(typeID.getInternal(), service.getServiceName(), timeout);
			}
		}
	}

	public void serviceAdded(ServiceEvent arg0) {
		if (jmdns != null) {
			try {
				fireServiceAdded(new ServiceContainerEvent(createIServiceInfoFromServiceEvent(arg0), getID()));
			} catch (final Exception e) {
				Trace.catching(JMDNSPlugin.PLUGIN_ID, JMDNSDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), "serviceAdded", e); //$NON-NLS-1$
			}
		}
	}

	public void serviceRemoved(ServiceEvent arg0) {
		if (jmdns != null) {
			try {
				fireServiceRemoved(new ServiceContainerEvent(createIServiceInfoFromServiceEvent(arg0), getID()));
			} catch (final Exception e) {
				Trace.catching(JMDNSPlugin.PLUGIN_ID, JMDNSDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), "serviceRemoved", e); //$NON-NLS-1$
			}
		}
	}

	public void serviceResolved(ServiceEvent arg0) {
		if (jmdns != null) {
			try {
				fireServiceResolved(new ServiceContainerEvent(createIServiceInfoFromServiceEvent(arg0), getID()));
			} catch (final Exception e) {
				Trace.catching(JMDNSPlugin.PLUGIN_ID, JMDNSDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), "serviceResolved", e); //$NON-NLS-1$
			}
		}
	}

	public void serviceTypeAdded(ServiceEvent arg0) {
		if (jmdns != null) {
			try {
				fireServiceTypeAdded(new ServiceContainerEvent(createIServiceInfoFromServiceEvent(arg0), getID()));
			} catch (final Exception e) {
				Trace.catching(JMDNSPlugin.PLUGIN_ID, JMDNSDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), "serviceTypeAdded", e); //$NON-NLS-1$
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
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServices(org.eclipse.ecf.discovery.identity.IServiceTypeID)
	 */
	public IServiceInfo[] getServices(IServiceTypeID type) {
		return getServices(type.getName());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#registerServiceType(org.eclipse.ecf.discovery.identity.IServiceTypeID)
	 */
	public void registerServiceType(IServiceTypeID type) {
		if (type instanceof JMDNSServiceTypeID) {
			final JMDNSServiceTypeID typeID = (JMDNSServiceTypeID) type;
			registerServiceType(typeID.getInternal());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#getConnectedID()
	 */
	public ID getConnectedID() {
		return this.targetID;
	}

	private IServiceInfo createIServiceInfoFromServiceEvent(ServiceEvent event) {
		final ServiceID sID = createServiceID(event.getType(), event.getName());
		final ServiceInfo sinfo = event.getInfo();
		if (sinfo != null) {
			return createIServiceInfoFromServiceInfo(sinfo);
		}
		final IServiceInfo newInfo = new JMDNSServiceInfo(null, sID, -1, -1, -1, new ServiceProperties());
		return newInfo;
	}

	private IServiceInfo createIServiceInfoFromServiceInfo(final ServiceInfo serviceInfo) {
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

	private ServiceID createServiceID(String type, String name) {
		ServiceID id = null;
		try {
			id = (ServiceID) IDFactory.getDefault().createID(JMDNSNamespace.NAME, new Object[] {type, name});
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
		final ServiceInfo si = new ServiceInfo(sID.getServiceTypeID().getName(), sID.getServiceName(), serviceInfo.getPort(), serviceInfo.getPriority(), serviceInfo.getWeight(), props);
		return si;
	}

}
