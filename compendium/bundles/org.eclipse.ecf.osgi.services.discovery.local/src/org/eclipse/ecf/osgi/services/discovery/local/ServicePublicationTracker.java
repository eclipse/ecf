/* 
 * Copyright (c) 2009 Siemens Enterprise Communications GmbH & Co. KG, 
 * Germany. All rights reserved.
 *
 * Siemens Enterprise Communications GmbH & Co. KG is a Trademark Licensee 
 * of Siemens AG.
 *
 * This material, including documentation and any related computer programs,
 * is protected by copyright controlled by Siemens Enterprise Communications 
 * GmbH & Co. KG and its licensors. All rights are reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this 
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.ecf.osgi.services.discovery.local;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ecf.osgi.services.discovery.ServiceEndpointDescription;
import org.eclipse.ecf.osgi.services.discovery.ServicePublication;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class ServicePublicationTracker implements ServiceTrackerCustomizer {

	private BundleContext context = null;

	private FileBasedDiscoveryImpl discovery = null;

	private Map /* <ServiceReference, ServiceEndpointDescription> */publicationAndSED = null;

	public ServicePublicationTracker(BundleContext ctx,
			FileBasedDiscoveryImpl disco) {
		context = ctx;
		discovery = disco;
		publicationAndSED = Collections.synchronizedMap(new HashMap());
	}

	/**
	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
	 */
	public Object addingService(ServiceReference arg0) {
		ServicePublication sp = publishServicePublication(arg0);
		return sp;
	}

	/**
	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
	 *      java.lang.Object)
	 */
	public void modifiedService(ServiceReference arg0, Object arg1) {
		unpublishServicePublication(arg0);
		publishServicePublication(arg0);
	}

	/**
	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
	 *      java.lang.Object)
	 */
	public void removedService(ServiceReference arg0, Object arg1) {
		unpublishServicePublication(arg0);
	}

	/**
	 * 
	 * @param arg0
	 * @return
	 */
	private ServicePublication publishServicePublication(ServiceReference arg0) {
		ServicePublication sp = (ServicePublication) context.getService(arg0);
		ServiceEndpointDescription sed = discovery
				.publishService(
						(Collection) arg0
								.getProperty(ServicePublication.SERVICE_INTERFACE_NAME),
						(Collection) arg0
								.getProperty(ServicePublication.SERVICE_INTERFACE_VERSION),
						(Collection) arg0
								.getProperty(ServicePublication.ENDPOINT_INTERFACE_NAME),
						(Map) arg0
								.getProperty(ServicePublication.SERVICE_PROPERTIES),
						FileBasedDiscoveryImpl.PROP_VAL_PUBLISH_STRATEGY_PUSH,
						(String) arg0
								.getProperty(ServicePublication.ENDPOINT_ID));
		publicationAndSED.put(arg0, sed);
		return sp;
	}

	/**
	 * 
	 * @param srvReference
	 *            the given reference to the service to unpublish
	 */
	private void unpublishServicePublication(ServiceReference srvReference) {
		discovery
				.unpublishService((ServiceEndpointDescription) publicationAndSED
						.get(srvReference));
		publicationAndSED.remove(srvReference);
	}
}
