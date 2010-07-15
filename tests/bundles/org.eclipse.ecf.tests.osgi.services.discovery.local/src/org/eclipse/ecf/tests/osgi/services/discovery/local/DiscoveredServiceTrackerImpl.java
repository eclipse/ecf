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
package org.eclipse.ecf.tests.osgi.services.discovery.local;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.eclipse.ecf.osgi.services.discovery.DiscoveredServiceNotification;
import org.eclipse.ecf.osgi.services.discovery.DiscoveredServiceTracker;

public class DiscoveredServiceTrackerImpl implements DiscoveredServiceTracker {

	private Collection descriptions = Collections
			.synchronizedSet(new HashSet());

	private int availNotifications = 0;
	private int unavailNotifications = 0;
	private int modifiedNotifications = 0;

	/**
	 * @return the availNotifications
	 */
	public int getAvailNotifications() {
		return availNotifications;
	}

	/**
	 * @return the unavailNotifications
	 */
	public int getUnavailNotifications() {
		return unavailNotifications;
	}

	public Collection getAvailableDescriptions() {
		return Collections.unmodifiableCollection(descriptions);
	}

	/**
	 * @return the modifiedNotifications
	 */
	public int getModifiedNotifications() {
		return modifiedNotifications;
	}

	public void clearLists() {
		if (descriptions != null) {
			descriptions.clear();
		}
	}

	public void serviceChanged(DiscoveredServiceNotification notification) {
		switch (notification.getType()) {
		case DiscoveredServiceNotification.AVAILABLE:
			System.out.println("["+this+"] "+"Available notified for "
					+ notification.getServiceEndpointDescription()
							.getProvidedInterfaces());
			descriptions.add(notification.getServiceEndpointDescription());
			availNotifications++;
			break;
		case DiscoveredServiceNotification.MODIFIED:
			System.out.println("["+this+"] "+"Modified notified for "
					+ notification.getServiceEndpointDescription()
							.getProvidedInterfaces());
			descriptions.remove(notification.getServiceEndpointDescription());
			descriptions.add(notification.getServiceEndpointDescription());
			modifiedNotifications++;
			break;
		case DiscoveredServiceNotification.UNAVAILABLE:
			System.out.println("["+this+"] "+"Unavailable notified for "
					+ notification.getServiceEndpointDescription()
							.getProvidedInterfaces());
			descriptions.remove(notification.getServiceEndpointDescription());
			unavailNotifications++;
			break;
		default:
			break;
		}
	}

}
