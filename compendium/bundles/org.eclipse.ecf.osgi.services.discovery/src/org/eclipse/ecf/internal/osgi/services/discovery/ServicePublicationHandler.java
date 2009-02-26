/*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.discovery;

import java.io.Serializable;
import java.net.*;
import java.util.*;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.util.ECFRuntimeException;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.discovery.*;
import org.eclipse.ecf.discovery.identity.*;
import org.eclipse.ecf.discovery.service.IDiscoveryService;
import org.eclipse.ecf.osgi.services.discovery.ECFServicePublication;
import org.eclipse.ecf.remoteservice.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.discovery.*;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class ServicePublicationHandler implements ServiceTrackerCustomizer {

	private Map serviceInfos = Collections.synchronizedMap(new HashMap());

	private final IServiceListener serviceListener = new IServiceListener() {
		public void serviceDiscovered(IServiceEvent anEvent) {
			handleServiceDiscovered(anEvent.getLocalContainerID(), anEvent
					.getServiceInfo());
		}

		public void serviceUndiscovered(IServiceEvent anEvent) {
			handleServiceUndiscovered(anEvent.getLocalContainerID(), anEvent
					.getServiceInfo());
		}
	};

	void handleServiceDiscovered(ID localContainerID, IServiceInfo serviceInfo) {
		IServiceID serviceID = serviceInfo.getServiceID();
		if (matchServiceID(serviceID)) {
			trace("handleOSGIServiceDiscovered", "serviceInfo=" + serviceInfo);
			DiscoveredServiceTracker[] discoveredTrackers = findMatchingDiscoveredServiceTrackers(serviceInfo);
			if (discoveredTrackers != null) {
				for (int i = 0; i < discoveredTrackers.length; i++) {
					discoveredTrackers[i]
							.serviceChanged(new DiscoveredServiceNotificationImpl(
									localContainerID,
									DiscoveredServiceNotification.AVAILABLE,
									serviceInfo));
				}
			}
		}
	}

	void handleServiceUndiscovered(ID localContainerID, IServiceInfo serviceInfo) {
		IServiceID serviceID = serviceInfo.getServiceID();
		if (matchServiceID(serviceID)) {
			trace("handleOSGIServiceUndiscovered", "serviceInfo=" + serviceInfo);
			DiscoveredServiceTracker[] discoveredTrackers = findMatchingDiscoveredServiceTrackers(serviceInfo);
			if (discoveredTrackers != null) {
				for (int i = 0; i < discoveredTrackers.length; i++) {
					discoveredTrackers[i]
							.serviceChanged(new DiscoveredServiceNotificationImpl(
									localContainerID,
									DiscoveredServiceNotification.UNAVAILABLE,
									serviceInfo));
				}
			}
		}
	}

	private DiscoveredServiceTracker[] findMatchingDiscoveredServiceTrackers(
			IServiceInfo serviceInfo) {
		ServiceReference[] sourceTrackers = Activator.getDefault()
				.getDiscoveredServiceTrackerReferences();
		if (sourceTrackers == null)
			return null;
		List matchingTrackers = new ArrayList();
		for (int i = 0; i < sourceTrackers.length; i++) {
			if (matchWithDiscoveredServiceInfo(sourceTrackers[i], serviceInfo))
				matchingTrackers.add(Activator.getDefault().getContext()
						.getService(sourceTrackers[i]));
		}
		return (DiscoveredServiceTracker[]) matchingTrackers
				.toArray(new DiscoveredServiceTracker[] {});
	}

	private boolean matchWithDiscoveredServiceInfo(
			ServiceReference serviceReference, IServiceInfo serviceInfo) {
		// TODO Auto-generated method stub
		// XXX for now match everything. See RFC119
		return true;
	}

	private boolean matchServiceID(IServiceID serviceId) {
		if (Arrays.asList(serviceId.getServiceTypeID().getServices()).contains(
				ECFServicePublication.SERVICE_TYPE))
			return true;
		return false;
	}

	public ServicePublicationHandler() {
	}

	public ServiceReference[] getPublishedServices() {
		return (ServiceReference[]) serviceInfos.keySet().toArray(
				new ServiceReference[] {});
	}

	IServiceInfo addServiceInfo(ServiceReference sr, IServiceInfo si) {
		return (IServiceInfo) serviceInfos.put(sr, si);
	}

	IServiceInfo removeServiceInfo(ServiceReference sr) {
		return (IServiceInfo) serviceInfos.remove(sr);
	}

	IServiceInfo getServiceInfo(ServiceReference sr) {
		return (IServiceInfo) serviceInfos.get(sr);
	}

	public Object addingService(ServiceReference reference) {
		handleServicePublication(reference);
		return Activator.getDefault().getContext().getService(reference);
	}

	/**
	 * @param reference
	 */
	private void handleServicePublication(ServiceReference reference) {
		// Get required service property "service.interface", which should be a
		// Collection of Strings
		Collection svcInterfaces = ServicePropertyUtils.getCollectionProperty(
				reference, ServicePublication.PROP_KEY_SERVICE_INTERFACE_NAME);
		// If it's not there, then we ignore this ServicePublication and return
		if (svcInterfaces == null) {
			trace(
					"handleServicePublication",
					"ignoring "
							+ reference
							+ ". ServicePublication.PROP_KEY_SERVICE_INTERFACE_NAME not set");
			return;
		}
		IServiceProperties discoveryServiceProperties = new ServiceProperties();

		discoveryServiceProperties.setPropertyString(
				ServicePublication.PROP_KEY_SERVICE_INTERFACE_NAME,
				ServicePropertyUtils.createStringFromCollection(svcInterfaces));

		// Get optional service.interface.version
		// Currently, in
		// org.eclipse.ecf.internal.osgi.services.distribution.EventHookImpl.getServicePublicationProperties(RSCAHolder,
		// ServiceReference, String[], IRemoteServiceRegistration)
		// we do not set these ServicePublication properties, so there's no use
		// in processing them
		// Collection interfaceVersions = ServicePropertyUtils
		// .getCollectionProperty(reference,
		// ServicePublication.PROP_KEY_SERVICE_INTERFACE_VERSION);
		// // Get osgi.remote.endpoint.interface
		// Collection endpointInterfaces = ServicePropertyUtils
		// .getCollectionProperty(reference,
		// ServicePublication.PROP_KEY_ENDPOINT_INTERFACE_NAME);
		// Get service properties
		Map servicePublicationServiceProperties = ServicePropertyUtils
				.getMapProperty(reference,
						ServicePublication.PROP_KEY_SERVICE_PROPERTIES);
		if (servicePublicationServiceProperties == null) {
			trace(
					"handleServicePublication",
					"ignoring "
							+ reference
							+ ". ServicePublication.PROP_KEY_SERVICE_PROPERTIES not set");
			return;
		}

		// Add them
		addPropertiesToDiscoveryServiceProperties(discoveryServiceProperties,
				servicePublicationServiceProperties);

		// Get endpoint ID
		String servicePublicationEndpointID = ServicePropertyUtils
				.getStringProperty(reference,
						ServicePublication.PROP_KEY_ENDPOINT_ID);
		if (servicePublicationEndpointID == null) {
			trace("handleServicePublication", "ignoring " + reference
					+ ". ServicePublication.PROP_KEY_ENDPOINT_ID not set");
			return;
		}
		discoveryServiceProperties.setPropertyString(
				ServicePublication.PROP_KEY_ENDPOINT_ID,
				servicePublicationEndpointID);

		// Get ECF properties
		String connectNamespaceName = ServicePropertyUtils.getStringProperty(
				reference, Constants.SERVICE_CONNECT_ID_NAMESPACE);
		if (connectNamespaceName == null) {
			trace("handleServicePublication", "ignoring " + reference
					+ ". Constants.SERVICE_CONNECT_ID_NAMESPACE not set");
			return;
		}
		discoveryServiceProperties.setPropertyString(
				Constants.SERVICE_CONNECT_ID_NAMESPACE, connectNamespaceName);

		String idnamespace = ServicePropertyUtils.getStringProperty(reference,
				Constants.SERVICE_IDFILTER_NAMESPACE);
		if (idnamespace == null) {
			trace("handleServicePublication", "ignoring " + reference
					+ ". Constants.SERVICE_IDFILTER_NAMESPACE not set");
			return;
		}
		discoveryServiceProperties.setPropertyString(
				Constants.SERVICE_IDFILTER_NAMESPACE, idnamespace);

		String rsnamespace = ServicePropertyUtils.getStringProperty(reference,
				Constants.SERVICE_NAMESPACE);
		if (rsnamespace == null) {
			trace("handleServicePublication", "ignoring " + reference
					+ ". Constants.SERVICE_NAMESPACE not set");
			return;
		}
		discoveryServiceProperties.setPropertyString(
				Constants.SERVICE_NAMESPACE, rsnamespace);

		Long remoteServiceID = (Long) reference
				.getProperty(Constants.SERVICE_ID);
		if (remoteServiceID == null) {
			trace("handleServicePublication", "ignoring " + reference
					+ ". Constants.SERVICE_ID not set");
			return;
		}

		discoveryServiceProperties.setProperty(Constants.SERVICE_ID,
				remoteServiceID);

		IServiceInfo svcInfo = null;
		try {
			IServiceID serviceID = createServiceID(
					servicePublicationServiceProperties, remoteServiceID);
			URI uri = createURI(servicePublicationEndpointID);

			svcInfo = new ServiceInfo(uri, serviceID,
					discoveryServiceProperties);

		} catch (IDCreateException e) {
			traceException("handleServicePublication", e);
		} catch (URISyntaxException e) {
			traceException("handleServicePublication", e);
		}
		if (svcInfo == null) {
			trace("addServicePublication",
					"svcInfo is null, so no service published");
			return;
		}
		publishService(reference, svcInfo);
	}

	private URI createURI(String servicePublicationEndpointID)
			throws URISyntaxException {
		boolean done = false;
		URI uri = null;
		String str = servicePublicationEndpointID;
		while (!done) {
			try {
				uri = new URI(str);
				if (!uri.isOpaque()) {
					done = true;
				} else {
					str = uri.getRawSchemeSpecificPart();
				}
			} catch (URISyntaxException e) {
				done = true;
			}
		}
		String scheme = ECFServicePublication.SERVICE_TYPE;
		int port = 32565;
		if (uri != null) {
			port = uri.getPort();
			if (port == -1)
				port = 32565;
		}
		String host = null;
		try {
			host = InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
			host = "localhost";
		}
		return new URI(scheme, null, host, port, null, null, null);
	}

	private void addPropertiesToDiscoveryServiceProperties(
			IServiceProperties discoveryServiceProperties,
			Map servicePublicationServiceProperties) {
		for (Iterator i = servicePublicationServiceProperties.keySet()
				.iterator(); i.hasNext();) {
			Object key = i.next();
			if (!(key instanceof String)) {
				trace("addPropertiesToDiscoveryServiceProperties",
						"skipping non-string key " + key);
				continue;
			}
			String keyStr = (String) key;
			Object val = servicePublicationServiceProperties.get(keyStr);
			if (val instanceof String) {
				discoveryServiceProperties.setPropertyString(keyStr,
						(String) val);
			} else if (val instanceof byte[]) {
				discoveryServiceProperties.setPropertyBytes(keyStr,
						(byte[]) val);
			} else if (val instanceof Serializable) {
				discoveryServiceProperties.setProperty(keyStr, val);
			}
		}
	}

	private IDiscoveryService discovery;

	private synchronized IDiscoveryService getDiscovery() {
		try {
			if (discovery == null) {
				discovery = Activator.getDefault().getDiscoveryService();
				discovery.addServiceListener(serviceListener);
			}
		} catch (InterruptedException e) {
			traceException("getDiscovery", e);
		}
		return discovery;
	}

	String getPropertyWithDefault(Map properties, String key, String def) {
		String val = (String) properties.get(key);
		return (val == null) ? def : val;
	}

	protected IServiceID createServiceID(Map servicePublicationProperties,
			Long rsvcid) throws IDCreateException {
		IDiscoveryService d = getDiscovery();
		if (d == null)
			return null;
		String namingAuthority = getPropertyWithDefault(
				servicePublicationProperties,
				ECFServicePublication.NAMING_AUTHORITY_PROP,
				IServiceTypeID.DEFAULT_NA);
		String scope = getPropertyWithDefault(servicePublicationProperties,
				ECFServicePublication.SCOPE_PROP,
				// IServiceTypeID.DEFAULT_SCOPE[0]);
				"local");
		String protocol = getPropertyWithDefault(servicePublicationProperties,
				ECFServicePublication.SERVICE_PROTOCOL_PROP,
				IServiceTypeID.DEFAULT_PROTO[0]);

		String serviceName = getPropertyWithDefault(
				servicePublicationProperties,
				ECFServicePublication.SERVICE_NAME_PROP,
				(ECFServicePublication.DEFAULT_SERVICE_NAME_PREFIX + rsvcid));
		String serviceType = "_" + ECFServicePublication.SERVICE_TYPE + "._"
				+ protocol + "." + scope + "._" + namingAuthority;
		return ServiceIDFactory.getDefault().createServiceID(
				discovery.getServicesNamespace(), serviceType, serviceName);
	}

	private void publishService(ServiceReference reference, IServiceInfo svcInfo) {
		synchronized (serviceInfos) {
			try {
				addServiceInfo(reference, svcInfo);
				trace("publishService", "publishing serviceReference="
						+ reference + ", svcInfo=" + svcInfo);
				discovery.registerService(svcInfo);
			} catch (ECFRuntimeException e) {
				traceException("publishService", e);
				removeServiceInfo(reference);
			}
		}
	}

	public void modifiedService(ServiceReference reference, Object service) {
		unpublishService(reference);
		handleServicePublication(reference);
	}

	public void removedService(ServiceReference reference, Object service) {
		unpublishService(reference);
	}

	private void unpublishService(ServiceReference reference) {
		synchronized (serviceInfos) {
			try {
				IServiceInfo svcInfo = removeServiceInfo(reference);
				if (svcInfo != null)
					discovery.unregisterService(svcInfo);
			} catch (ECFRuntimeException e) {
				traceException("publishService", e);
			}
		}
	}

	protected void trace(String methodName, String message) {
		Trace.trace(Activator.PLUGIN_ID, DebugOptions.SVCPUBHANDLERDEBUG, this
				.getClass(), methodName, message);
	}

	protected void traceException(String string, Throwable e) {
		Trace.catching(Activator.PLUGIN_ID, DebugOptions.EXCEPTIONS_CATCHING,
				this.getClass(), string, e);
	}

	public void dispose() {
		if (discovery != null) {
			discovery.removeServiceListener(serviceListener);
			for (Iterator i = serviceInfos.keySet().iterator(); i.hasNext();) {
				ServiceReference sr = (ServiceReference) i.next();
				unpublishService(sr);
			}
			serviceInfos.clear();
			discovery = null;
		}
	}
}
