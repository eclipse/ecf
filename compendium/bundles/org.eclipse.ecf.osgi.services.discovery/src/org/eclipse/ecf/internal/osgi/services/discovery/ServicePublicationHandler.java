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
import org.eclipse.ecf.osgi.services.discovery.ECFServicePublication;
import org.eclipse.ecf.provider.discovery.CompositeServiceContainerEvent;
import org.eclipse.ecf.remoteservice.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.discovery.*;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class ServicePublicationHandler implements ServiceTrackerCustomizer {

	private Map serviceInfos = Collections.synchronizedMap(new HashMap());

	private final IServiceListener serviceListener = new IServiceListener() {
		public void serviceDiscovered(IServiceEvent anEvent) {
			handleServiceDiscovered(anEvent);
		}

		public void serviceUndiscovered(IServiceEvent anEvent) {
			handleServiceUndiscovered(anEvent);
		}
	};

	void handleServiceDiscovered(IServiceEvent event) {
		IServiceInfo serviceInfo = event.getServiceInfo();
		IServiceID serviceID = serviceInfo.getServiceID();
		ID localContainerID = event.getLocalContainerID();
		// Set the original container ID to the re
		ID originalLocalContainerID = localContainerID;
		// If it's a composite container, then there is also the original
		// container ID
		if (event instanceof CompositeServiceContainerEvent) {
			originalLocalContainerID = ((CompositeServiceContainerEvent) event)
					.getOriginalLocalContainerID();
		}
		trace("handleOSGIServiceDiscovered", "localContainerID="
				+ localContainerID + ",originalLocalContainerID="
				+ originalLocalContainerID + " serviceInfo=" + serviceInfo);
		if (matchServiceID(serviceID)) {
			trace("handleOSGIServiceDiscovered matched", "localContainerID="
					+ localContainerID + ",originalLocalContainerID="
					+ originalLocalContainerID + " serviceInfo=" + serviceInfo);
			DiscoveredServiceTracker[] discoveredTrackers = findMatchingDiscoveredServiceTrackers(serviceInfo);
			if (discoveredTrackers != null) {
				for (int i = 0; i < discoveredTrackers.length; i++) {
					discoveredTrackers[i]
							.serviceChanged(new DiscoveredServiceNotificationImpl(
									localContainerID, originalLocalContainerID,
									DiscoveredServiceNotification.AVAILABLE,
									serviceInfo));
				}
			}
		}
	}

	void handleServiceUndiscovered(IServiceEvent event) {
		IServiceInfo serviceInfo = event.getServiceInfo();
		IServiceID serviceID = serviceInfo.getServiceID();
		ID localContainerID = event.getLocalContainerID();
		// Set the original container ID to the re
		ID originalLocalContainerID = localContainerID;
		// If it's a composite container, then there is also the original
		// container ID
		if (event instanceof CompositeServiceContainerEvent) {
			originalLocalContainerID = ((CompositeServiceContainerEvent) event)
					.getOriginalLocalContainerID();
		}
		if (matchServiceID(serviceID)) {
			trace("handleOSGIServiceUndiscovered", "localContainerID="
					+ localContainerID + ",originalLocalContainerID="
					+ originalLocalContainerID + " serviceInfo=" + serviceInfo);
			DiscoveredServiceTracker[] discoveredTrackers = findMatchingDiscoveredServiceTrackers(serviceInfo);
			if (discoveredTrackers != null) {
				for (int i = 0; i < discoveredTrackers.length; i++) {
					discoveredTrackers[i]
							.serviceChanged(new DiscoveredServiceNotificationImpl(
									localContainerID, originalLocalContainerID,
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

		// Get required service RFC 119 property "service.interface", which
		// should be a
		// Collection of Strings
		Collection svcInterfaces = ServicePropertyUtils.getCollectionProperty(
				reference, ServicePublication.PROP_KEY_SERVICE_INTERFACE_NAME);
		// If it's not there, then we ignore this ServicePublication and return
		if (svcInterfaces == null) {
			logError(
					"handleServicePublication",
					"ignoring "
							+ reference
							+ ". ServicePublication.PROP_KEY_SERVICE_INTERFACE_NAME not set",
					null);
			return;
		}
		IServiceProperties discoveryServiceProperties = new ServiceProperties();
		discoveryServiceProperties.setPropertyString(
				ServicePublication.PROP_KEY_SERVICE_INTERFACE_NAME,
				ServicePropertyUtils.createStringFromCollection(svcInterfaces));

		// We also use the optional RFC 119 property PROP_KEY_SERVICE_PROPERTIES
		Map servicePublicationServiceProperties = ServicePropertyUtils
				.getMapProperty(reference,
						ServicePublication.PROP_KEY_SERVICE_PROPERTIES);
		if (servicePublicationServiceProperties == null) {
			logError(
					"handleServicePublication",
					"ignoring "
							+ reference
							+ ". ServicePublication.PROP_KEY_SERVICE_PROPERTIES not set",
					null);
			return;
		}
		// Add them

		addPropertiesToDiscoveryServiceProperties(discoveryServiceProperties,
				servicePublicationServiceProperties);

		// See EventHookImpl.getServicePublicationProperties()
		// Get and then serialize and set
		// ECFServicePublication.PROP_KEY_ENDPOINT_CONTAINERID
		ID endpointContainerID = (ID) reference
				.getProperty(ECFServicePublication.PROP_KEY_ENDPOINT_CONTAINERID);
		if (endpointContainerID == null) {
			logError(
					"handleServicePublication",
					"ignoring "
							+ reference
							+ ". ECFServicePublication.PROP_KEY_ENDPOINT_CONTAINERID not set",
					null);
			return;
		}
		// Add endpoint container id.toExternalForm().getBytes...so AS byte []
		discoveryServiceProperties.setPropertyBytes(
				ECFServicePublication.PROP_KEY_ENDPOINT_CONTAINERID,
				endpointContainerID.toExternalForm().getBytes());

		// Add container id namespace name
		String namespace = endpointContainerID.getNamespace().getName();
		discoveryServiceProperties.setPropertyString(
				ECFServicePublication.PROP_KEY_ENDPOINT_CONTAINERID_NAMESPACE,
				namespace);

		// remote service namespace
		String rsnamespace = ServicePropertyUtils.getStringProperty(reference,
				Constants.SERVICE_NAMESPACE);
		if (rsnamespace == null) {
			logError("handleServicePublication", "ignoring " + reference
					+ ". Constants.SERVICE_NAMESPACE not set", null);
			return;
		}
		discoveryServiceProperties.setPropertyString(
				Constants.SERVICE_NAMESPACE, rsnamespace);

		// remote service id

		Long remoteServiceID = (Long) reference
				.getProperty(Constants.SERVICE_ID);
		if (remoteServiceID == null) {
			logError("handleServicePublication", "ignoring " + reference
					+ ". Constants.SERVICE_ID not set", null);
			return;
		}
		discoveryServiceProperties.setProperty(Constants.SERVICE_ID,
				remoteServiceID);

		IServiceInfo svcInfo = null;
		try {
			IServiceID serviceID = createServiceID(
					servicePublicationServiceProperties, remoteServiceID);
			URI uri = createURI(endpointContainerID);

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

	private void logError(String method, String message, Throwable t) {
		// TODO log exception

		traceException(method + ":" + message, t);
	}

	private URI createURI(ID endpointContainerID) throws URISyntaxException {
		boolean done = false;
		URI uri = null;
		String str = endpointContainerID.getName();
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

	private IDiscoveryLocator locator;
	private IDiscoveryAdvertiser advertiser;

	private synchronized IDiscoveryLocator getLocator() {
		try {
			if (locator == null) {
				locator = Activator.getDefault().getLocator();
				locator.addServiceListener(serviceListener);
			}
		} catch (InterruptedException e) {
			traceException("getLocator", e);
		}
		return locator;
	}

	private synchronized IDiscoveryAdvertiser getAdvertiser() {
		try {
			if (advertiser == null) {
				advertiser = Activator.getDefault().getAdvertiser();
			}
		} catch (InterruptedException e) {
			traceException("getAdvertiser", e);
		}
		return advertiser;
	}

	String getPropertyWithDefault(Map properties, String key, String def) {
		String val = (String) properties.get(key);
		return (val == null) ? def : val;
	}

	protected IServiceID createServiceID(Map servicePublicationProperties,
			Long rsvcid) throws IDCreateException {
		IDiscoveryLocator l = getLocator();
		if (l == null)
			return null;
		String namingAuthority = getPropertyWithDefault(
				servicePublicationProperties,
				ECFServicePublication.NAMING_AUTHORITY_PROP,
				IServiceTypeID.DEFAULT_NA);
		String scope = getPropertyWithDefault(servicePublicationProperties,
				ECFServicePublication.SCOPE_PROP,
				IServiceTypeID.DEFAULT_SCOPE[0]);
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
				l.getServicesNamespace(), serviceType, serviceName);
	}

	private void publishService(ServiceReference reference, IServiceInfo svcInfo) {
		synchronized (serviceInfos) {
			try {
				addServiceInfo(reference, svcInfo);
				trace("publishService", "publishing serviceReference="
						+ reference + ", svcInfo=" + svcInfo);
				getAdvertiser().registerService(svcInfo);
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
					getAdvertiser().unregisterService(svcInfo);
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
		if (locator != null) {
			locator.removeServiceListener(serviceListener);
			for (Iterator i = serviceInfos.keySet().iterator(); i.hasNext();) {
				ServiceReference sr = (ServiceReference) i.next();
				unpublishService(sr);
			}
			serviceInfos.clear();
			locator = null;
		}
		if (locator != null) {
			locator = null;
		}
	}
}
