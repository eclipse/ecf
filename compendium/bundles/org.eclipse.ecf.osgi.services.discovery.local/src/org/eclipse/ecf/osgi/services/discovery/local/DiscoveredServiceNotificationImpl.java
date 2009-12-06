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

import org.eclipse.ecf.osgi.services.discovery.DiscoveredServiceNotification;
import org.eclipse.ecf.osgi.services.discovery.ServiceEndpointDescription;

public class DiscoveredServiceNotificationImpl implements
		DiscoveredServiceNotification {

	private ServiceEndpointDescription descr;
	private int type;
	private Collection/* String */filters;
	private Collection/* String */interfaces;

	public DiscoveredServiceNotificationImpl(ServiceEndpointDescription sed,
			int notificationType, Collection matchingFilters,
			Collection matchingInterfaces) {
		descr = sed;
		type = notificationType;
		filters = matchingFilters;
		interfaces = matchingInterfaces;
	}

	/**
	 * @see org.eclipse.ecf.osgi.services.discovery.DiscoveredServiceNotification#getServiceEndpointDescription()
	 */
	public ServiceEndpointDescription getServiceEndpointDescription() {
		return descr;
	}

	/**
	 * @see org.eclipse.ecf.osgi.services.discovery.DiscoveredServiceNotification#getType()
	 */
	public int getType() {
		return type;
	}

	/**
	 * @see org.eclipse.ecf.osgi.services.discovery.DiscoveredServiceNotification#getFilters()
	 */
	public Collection getFilters() {
		return filters;
	}

	/**
	 * @see org.eclipse.ecf.osgi.services.discovery.DiscoveredServiceNotification#getInterfaces()
	 */
	public Collection getInterfaces() {
		return interfaces;
	}
}
