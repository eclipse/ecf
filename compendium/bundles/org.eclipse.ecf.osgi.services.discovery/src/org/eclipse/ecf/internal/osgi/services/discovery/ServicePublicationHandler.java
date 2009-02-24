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

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.util.ECFRuntimeException;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.discovery.*;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.identity.ServiceIDFactory;
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
		ServicePublication servicePublication = (ServicePublication) Activator
				.getDefault().getContext().getService(reference);
		addServicePublication(reference, servicePublication);
		return servicePublication;
	}

	private void addServicePublication(ServiceReference reference,
			ServicePublication servicePublication) {
		// Get required service property "service.interface", which should be a
		// Collection of Strings
		Collection svcInterfaces = ServicePropertyUtils.getCollectionProperty(
				reference, ServicePublication.PROP_KEY_SERVICE_INTERFACE_NAME);
		// If it's not there, then we ignore this publication and return
		if (svcInterfaces == null) {
			trace(
					"createServiceInfo",
					"svcInterfaces="
							+ svcInterfaces
							+ " is null or not instance of Collection as specified by RFC119");
			return;
		}
		// Get service.interface.version
		Collection interfaceVersions = ServicePropertyUtils
				.getCollectionProperty(reference,
						ServicePublication.PROP_KEY_SERVICE_INTERFACE_VERSION);
		// Get osgi.remote.endpoint.interface
		Collection endpointInterfaces = ServicePropertyUtils
				.getCollectionProperty(reference,
						ServicePublication.PROP_KEY_ENDPOINT_INTERFACE_NAME);
		// Get all service properties
		Map svcProperties = ServicePropertyUtils.getMapProperty(reference,
				ServicePublication.PROP_KEY_SERVICE_PROPERTIES);
		URL location = ServicePropertyUtils.getURLProperty(reference,
				ServicePublication.PROP_KEY_ENDPOINT_LOCATION);
		String id = ServicePropertyUtils.getStringProperty(reference,
				ServicePublication.PROP_KEY_ENDPOINT_ID);
		IServiceInfo svcInfo = null;
		try {
			svcInfo = createServiceInfo(reference, svcInterfaces,
					interfaceVersions, endpointInterfaces, svcProperties,
					location, id);
		} catch (IDCreateException e) {
			traceException("addServicePublication", e);
		}
		if (svcInfo == null) {
			trace("addServicePublication",
					"svcInfo is null, so no service published");
		} else {
			publishService(reference, svcInfo);
		}
	}

	protected IServiceInfo createServiceInfo(ServiceReference serviceReference,
			Collection svcInterfaces, Collection interfaceVersions,
			Collection endpointInterfaces, Map svcProperties, URL location,
			String id) throws IDCreateException {
		IServiceID serviceID = createServiceID(serviceReference);
		if (serviceID == null)
			return null;
		URI uri = createURI(location);
		IServiceProperties serviceProperties = createServiceProperties(
				svcInterfaces, interfaceVersions, endpointInterfaces,
				svcProperties, location, id);
		// ECF remote service property
		// Specify container factory name
		String serviceContainerFactoryName = (String) serviceReference
				.getProperty(Constants.SERVICE_CONTAINER_FACTORY_NAME);
		if (serviceContainerFactoryName != null) {
			serviceProperties.setPropertyString(
					Constants.SERVICE_CONTAINER_FACTORY_NAME,
					serviceContainerFactoryName);
		}
		return new ServiceInfo(uri, serviceID, serviceProperties);
	}

	protected IServiceProperties createServiceProperties(
			Collection svcInterfaces, Collection interfaceVersions,
			Collection endpointInterfaces, Map svcProperties, URL location,
			String id) {
		ServiceProperties serviceProperties = new ServiceProperties();
		if (svcInterfaces != null)
			serviceProperties.setProperty(
					ServicePublication.PROP_KEY_SERVICE_INTERFACE_NAME,
					ServicePropertyUtils
							.createStringFromCollection(svcInterfaces));
		if (interfaceVersions != null)
			serviceProperties.setProperty(
					ServicePublication.PROP_KEY_SERVICE_INTERFACE_VERSION,
					ServicePropertyUtils
							.createStringFromCollection(interfaceVersions));
		if (endpointInterfaces != null)
			serviceProperties.setProperty(
					ServicePublication.PROP_KEY_ENDPOINT_INTERFACE_NAME,
					ServicePropertyUtils
							.createStringFromCollection(endpointInterfaces));
		if (svcProperties != null) {
			for (Iterator i = svcProperties.keySet().iterator(); i.hasNext();) {
				String key = (String) i.next();
				Object val = svcProperties.get(key);
				if (val instanceof String)
					serviceProperties.setProperty(key, (String) val);
				else if (val instanceof byte[])
					serviceProperties.setProperty(key, (byte[]) val);
				else if (val instanceof Collection)
					serviceProperties.setProperty(key, ServicePropertyUtils
							.createStringFromCollection((Collection) val));
				else if (val instanceof String[])
					serviceProperties.setProperty(key, Arrays
							.asList((String[]) val));
			}
		}
		if (location != null)
			serviceProperties.setProperty(
					ServicePublication.PROP_KEY_ENDPOINT_LOCATION, location
							.toExternalForm());
		if (id != null)
			serviceProperties.setProperty(
					ServicePublication.PROP_KEY_ENDPOINT_ID, id);
		return serviceProperties;
	}

	protected URI createURI(URL location) {
		String locationStr = null;
		try {
			locationStr = (location == null) ? "unknown" : URLEncoder.encode(
					location.toExternalForm(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// should not happen
		}
		return URI.create(ECFServicePublication.SERVICE_TYPE
				+ ServicePropertyUtils.PROTOCOL_SEPARATOR + locationStr);
	}

	private void traceException(String string, Throwable e) {
		Trace.catching(Activator.PLUGIN_ID, DebugOptions.EXCEPTIONS_CATCHING,
				this.getClass(), string, e);
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

	protected IServiceID createServiceID(ServiceReference serviceReference)
			throws IDCreateException {
		IDiscoveryService d = getDiscovery();
		if (d == null)
			return null;
		String namingAuthority = ServicePropertyUtils.getStringProperty(
				serviceReference, ECFServicePublication.NAMING_AUTHORITY_PROP,
				ECFServicePublication.DEFAULT_NAMING_AUTHORITY);
		String scope = ServicePropertyUtils.getStringProperty(serviceReference,
				ECFServicePublication.SCOPE_PROP,
				ECFServicePublication.DEFAULT_SCOPE);
		String protocol = ServicePropertyUtils.getStringProperty(
				serviceReference, ECFServicePublication.SERVICE_PROTOCOL_PROP,
				ECFServicePublication.DEFAULT_SERVICE_PROTOCOL);
		String serviceName = ServicePropertyUtils.getStringProperty(
				serviceReference, ECFServicePublication.SERVICE_NAME_PROP,
				getDefaultServiceName(serviceReference));
		String serviceType = "_" + ECFServicePublication.SERVICE_TYPE + "._"
				+ protocol + "." + scope + "." + namingAuthority;
		return ServiceIDFactory.getDefault().createServiceID(
				discovery.getServicesNamespace(), serviceType, serviceName);
	}

	private String getDefaultServiceName(ServiceReference serviceReference) {
		return ECFServicePublication.DEFAULT_SERVICE_NAME_PREFIX
				+ serviceReference
						.getProperty(org.osgi.framework.Constants.SERVICE_ID);
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
		addServicePublication(reference, (ServicePublication) service);
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
