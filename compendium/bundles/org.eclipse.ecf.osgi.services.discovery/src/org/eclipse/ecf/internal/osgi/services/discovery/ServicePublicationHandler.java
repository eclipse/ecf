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
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.discovery.IServiceEvent;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.IServiceListener;
import org.eclipse.ecf.discovery.IServiceProperties;
import org.eclipse.ecf.discovery.ServiceInfo;
import org.eclipse.ecf.discovery.ServiceProperties;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.discovery.identity.ServiceIDFactory;
import org.eclipse.ecf.discovery.service.IDiscoveryService;
import org.eclipse.ecf.osgi.services.discovery.ServiceConstants;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.discovery.ServicePublication;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class ServicePublicationHandler implements ServiceTrackerCustomizer {

	private IDiscoveryService discovery;
	private final String serviceNamePrefix;
	private Map serviceInfos = Collections.synchronizedMap(new HashMap());

	private final IServiceListener serviceListener = new IServiceListener() {
		public void serviceDiscovered(IServiceEvent anEvent) {
			handleServiceDiscovered(anEvent);
		}

		public void serviceUndiscovered(IServiceEvent anEvent) {
			handleServiceUndiscovered(anEvent);
		}
	};

	void handleServiceDiscovered(IServiceEvent anEvent) {
		// TODO Auto-generated method stub

	}

	void handleServiceUndiscovered(IServiceEvent anEvent) {
		// TODO Auto-generated method stub

	}

	public ServicePublicationHandler(IDiscoveryService discovery,
			String serviceNamePrefix) {
		Assert.isNotNull(discovery);
		this.discovery = discovery;
		this.discovery.addServiceListener(serviceListener);
		Assert.isNotNull(serviceNamePrefix);
		this.serviceNamePrefix = serviceNamePrefix;
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
		if (svcInterfaces == null) {
			trace(
					"createServiceInfo",
					"svcInterfaces="
							+ svcInterfaces
							+ " is null or not instance of Collection as specified by RFC119");
			return;
		}
		Collection interfaceVersions = ServicePropertyUtils
				.getCollectionProperty(reference,
						ServicePublication.PROP_KEY_SERVICE_INTERFACE_VERSION);
		Collection endpointInterfaces = ServicePropertyUtils
				.getCollectionProperty(reference,
						ServicePublication.PROP_KEY_ENDPOINT_INTERFACE_NAME);
		Map svcProperties = ServicePropertyUtils.getMapProperty(reference,
				ServicePublication.PROP_KEY_SERVICE_PROPERTIES);
		URL location = ServicePropertyUtils.getURLProperty(reference,
				ServicePublication.PROP_KEY_ENDPOINT_LOCATION);
		String id = ServicePropertyUtils.getStringProperty(reference,
				ServicePublication.PROP_KEY_ENDPOINT_ID);
		ServiceInfo svcInfo = createServiceInfo(reference, svcInterfaces,
				interfaceVersions, endpointInterfaces, svcProperties, location,
				id);
		if (svcInfo == null) {
			trace("addServicePublication",
					"svcInfo is null, so no service published");
		} else {
			publishService(reference, svcInfo);
		}
	}

	private ServiceInfo createServiceInfo(ServiceReference serviceReference,
			Collection svcInterfaces, Collection interfaceVersions,
			Collection endpointInterfaces, Map svcProperties, URL location,
			String id) {
		IServiceID serviceID = createServiceID(serviceReference);
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

	private URI createURI(URL location) {
		String locationStr = null;
		try {
			locationStr = (location == null) ? "" : URLEncoder.encode(location
					.toExternalForm(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// should not happen
		}
		return URI.create(ServiceConstants.PROTOCOL
				+ ServiceConstants.PROTOCOL_SEPARATOR + locationStr);
	}

	private void traceException(String string, Throwable e) {
		Trace.catching(Activator.PLUGIN_ID, DebugOptions.EXCEPTIONS_CATCHING,
				this.getClass(), string, e);
	}

	private IServiceID createServiceID(ServiceReference serviceReference) {
		if (discovery == null)
			return null;
		String serviceName = serviceNamePrefix
				+ serviceReference.getProperty(Constants.SERVICE_ID);
		return ServiceIDFactory.getDefault().createServiceID(
				discovery.getServicesNamespace(), getDiscoveryType(),
				serviceName);
	}

	private IServiceTypeID getDiscoveryType() {
		// TODO Auto-generated method stub
		return null;
	}

	private void publishService(ServiceReference reference, IServiceInfo svcInfo) {
		synchronized (serviceInfos) {
			try {
				addServiceInfo(reference, svcInfo);
				discovery.registerService(svcInfo);
			} catch (ECFException e) {
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
			} catch (ECFException e) {
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
