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

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.IServiceProperties;
import org.eclipse.ecf.discovery.ServiceInfo;
import org.eclipse.ecf.discovery.ServiceProperties;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.identity.ServiceIDFactory;
import org.eclipse.ecf.discovery.service.IDiscoveryService;
import org.osgi.framework.ServiceReference;
import org.osgi.service.discovery.ServicePublication;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class ServicePublicationCustomizer implements ServiceTrackerCustomizer {

	public static final String SERVICES_DISCOVERY_TYPE = "_osgiservices";
	public static final String DEFAULT_URI_SCHEME = "osgiservices";

	private static long nextServiceID = 0L;
	
	private Map serviceInfos = Collections.synchronizedMap(new HashMap());

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
		Collection svcInterfaces = getCollectionProperty(reference,
				ServicePublication.PROP_KEY_SERVICE_INTERFACE_NAME);
		if (svcInterfaces == null) {
			trace(
					"createServiceInfo",
					"svcInterfaces="
							+ svcInterfaces
							+ " is null or not instance of Collection as specified by RFC119");
			return;
		}
		Collection interfaceVersions = getCollectionProperty(reference,
				ServicePublication.PROP_KEY_SERVICE_INTERFACE_VERSION);
		Collection endpointInterfaces = getCollectionProperty(reference,
				ServicePublication.PROP_KEY_ENDPOINT_INTERFACE_NAME);
		Map svcProperties = getMapProperty(reference,
				ServicePublication.PROP_KEY_SERVICE_PROPERTIES);
		URL location = getURLProperty(reference,
				ServicePublication.PROP_KEY_ENDPOINT_LOCATION);
		String id = getStringProperty(reference,
				ServicePublication.PROP_KEY_ENDPOINT_ID);
		ServiceInfo svcInfo = createServiceInfo(svcInterfaces,
				interfaceVersions, endpointInterfaces, svcProperties, location,
				id);
		if (svcInfo == null) {
			trace("addServicePublication",
					"svcInfo is null, so no service published");
		} else {
			publishService(reference, svcInfo);
		}
	}

	private ServiceInfo createServiceInfo(Collection svcInterfaces,
			Collection interfaceVersions, Collection endpointInterfaces,
			Map svcProperties, URL location, String id) {
		IServiceID serviceID = createServiceID(id);
		if (serviceID == null)
			return null;
		URI uri = createURI(location);
		IServiceProperties serviceProperties = createServiceProperties(
				svcInterfaces, interfaceVersions, endpointInterfaces,
				svcProperties, location, id);
		return new ServiceInfo(uri, serviceID, serviceProperties);
	}

	private IServiceProperties createServiceProperties(
			Collection svcInterfaces, Collection interfaceVersions,
			Collection endpointInterfaces, Map svcProperties, URL location,
			String id) {
		ServiceProperties serviceProperties = new ServiceProperties();
		if (svcInterfaces != null)
			serviceProperties.setProperty(
					ServicePublication.PROP_KEY_SERVICE_INTERFACE_NAME,
					createStringFromCollection(svcInterfaces));
		if (interfaceVersions != null)
			serviceProperties.setProperty(
					ServicePublication.PROP_KEY_SERVICE_INTERFACE_VERSION,
					createStringFromCollection(interfaceVersions));
		if (endpointInterfaces != null)
			serviceProperties.setProperty(
					ServicePublication.PROP_KEY_ENDPOINT_INTERFACE_NAME,
					createStringFromCollection(endpointInterfaces));
		if (svcProperties != null) {
			for (Iterator i = svcProperties.keySet().iterator(); i.hasNext();) {
				String key = (String) i.next();
				Object val = svcProperties.get(key);
				if (val instanceof String)
					serviceProperties.setProperty(key, (String) val);
				else if (val instanceof byte[])
					serviceProperties.setProperty(key, (byte[]) val);
				else
					serviceProperties.setProperty(key, val);
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

	private String createStringFromCollection(Collection svcInterfaces) {
		StringBuffer result = new StringBuffer();
		for (Iterator i = svcInterfaces.iterator(); i.hasNext();) {
			String item = (String) i.next();
			result.append(item);
			if (i.hasNext())
				result.append(",");
		}
		return result.toString();
	}

	private URI createURI(URL location) {
		if (location == null)
			return createDefaultURI();
		return URI.create(DEFAULT_URI_SCHEME + ":" + location.toExternalForm());
	}

	private void traceException(String string, Throwable e) {
		Trace.catching(Activator.PLUGIN_ID, DebugOptions.EXCEPTIONS_CATCHING,
				this.getClass(), string, e);
	}

	private URI createDefaultURI() {
		return URI.create("osgiservices:");
	}

	private IServiceID createServiceID(String id) {
		IDiscoveryService discovery = Activator.getDefault()
				.getDiscoveryService();
		if (discovery == null)
			return null;
		String serviceName = (id == null || "".equals(id)) ? createDefaultServiceName()
				: id;
		return ServiceIDFactory.getDefault().createServiceID(
				discovery.getServicesNamespace(), SERVICES_DISCOVERY_TYPE,
				serviceName);
	}

	private String createDefaultServiceName() {
		return DEFAULT_URI_SCHEME+"."+nextServiceID++;
	}

	private void registerServiceInfo(IDiscoveryService discovery, ServiceReference reference, IServiceInfo svcInfo) {
		synchronized (serviceInfos) {
			try {
				addServiceInfo(reference, svcInfo);
				discovery.registerService(svcInfo);
			} catch (ECFException e) {
				traceException("publishService",e);
				removeServiceInfo(reference);
			}
		}
	}

	private void unregisterServiceInfo(IDiscoveryService discovery, ServiceReference reference) {
		synchronized (serviceInfos) {
			try {
				IServiceInfo svcInfo = removeServiceInfo(reference);
				if (svcInfo != null) discovery.unregisterService(svcInfo);
			} catch (ECFException e) {
				traceException("publishService",e);
			}
		}
	}

	private void publishService(ServiceReference reference, IServiceInfo svcInfo) {
		IDiscoveryService discovery = Activator.getDefault().getDiscoveryService();
		if (discovery == null) {
			trace("publishService","no discovery available...cannot publish svcInfo="+svcInfo+" for serviceReference="+reference);
			return;
		}
		registerServiceInfo(discovery,reference,svcInfo);
	}

	private String getStringProperty(ServiceReference reference, String propKey) {
		Object val = reference.getProperty(propKey);
		if (val == null || !(val instanceof String))
			return null;
		return (String) val;
	}

	private URL getURLProperty(ServiceReference reference, String propKey) {
		Object val = reference.getProperty(propKey);
		if (val == null || !(val instanceof URL))
			return null;
		return (URL) val;
	}

	private Map getMapProperty(ServiceReference reference,
			String propKeyServiceProperties) {
		Object val = reference.getProperty(propKeyServiceProperties);
		if (val == null || !(val instanceof Map))
			return null;
		return (Map) val;
	}

	private Collection getCollectionProperty(ServiceReference reference,
			String propName) {
		Object val = reference.getProperty(propName);
		if (val == null || !(val instanceof Collection))
			return null;
		return (Collection) val;
	}

	public void modifiedService(ServiceReference reference, Object service) {
		unpublishService(reference);
		addServicePublication(reference,(ServicePublication) service);
	}

	public void removedService(ServiceReference reference, Object service) {
		unpublishService(reference);
	}

	private void unpublishService(ServiceReference reference) {
		IDiscoveryService discovery = Activator.getDefault().getDiscoveryService();
		if (discovery == null) {
			trace("unpublishService","no discovery available...cannot unpublish for serviceReference="+reference);
			return;
		}
		unregisterServiceInfo(discovery,reference);
	}

	protected void trace(String methodName, String message) {
		Trace.trace(Activator.PLUGIN_ID, DebugOptions.SVCPUBHANDLERDEBUG, this
				.getClass(), methodName, message);
	}

}
