/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.remoteserviceadmin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.discovery.IDiscoveryLocator;
import org.eclipse.ecf.discovery.IServiceEvent;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.IServiceListener;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.DiscoveredEndpointDescription;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.IDiscoveredEndpointDescriptionFactory;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteConstants;

class LocatorServiceListener implements IServiceListener {

	private Object listenerLock = new Object();
	private IDiscoveryLocator locator;
	private DiscoveryImpl discovery;

	private List<org.osgi.service.remoteserviceadmin.EndpointDescription> discoveredEndpointDescriptions = new ArrayList();

	public LocatorServiceListener(DiscoveryImpl discovery) {
		this(discovery, null);
	}

	public LocatorServiceListener(DiscoveryImpl discovery,
			IDiscoveryLocator locator) {
		this.discovery = discovery;
		this.locator = locator;
	}

	public void serviceDiscovered(IServiceEvent anEvent) {
		handleService(anEvent.getServiceInfo(), true);
	}

	public void serviceUndiscovered(IServiceEvent anEvent) {
		handleService(anEvent.getServiceInfo(), false);
	}

	private boolean matchServiceID(IServiceID serviceId) {
		if (Arrays.asList(serviceId.getServiceTypeID().getServices()).contains(
				RemoteConstants.SERVICE_TYPE))
			return true;
		return false;
	}

	void handleService(IServiceInfo serviceInfo, boolean discovered) {
		IServiceID serviceID = serviceInfo.getServiceID();
		if (matchServiceID(serviceID))
			handleOSGiServiceEndpoint(serviceID, serviceInfo, true);
	}

	private void handleOSGiServiceEndpoint(IServiceID serviceId,
			IServiceInfo serviceInfo, boolean discovered) {
		if (locator == null)
			return;
		DiscoveredEndpointDescription discoveredEndpointDescription = getDiscoveredEndpointDescription(
				serviceId, serviceInfo, discovered);
		if (discoveredEndpointDescription != null) {
			handleEndpointDescription(
					discoveredEndpointDescription.getEndpointDescription(),
					discovered);
		} else {
			logWarning("handleOSGiServiceEvent discoveredEndpointDescription is null for service info="
					+ serviceInfo + ",discovered=" + discovered);
		}
	}

	public void handleEndpointDescription(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription,
			boolean discovered) {
		synchronized (listenerLock) {
			if (discovered)
				discoveredEndpointDescriptions.add(endpointDescription);
			else
				discoveredEndpointDescriptions.remove(endpointDescription);

			discovery.queueEndpointDescription(endpointDescription, discovered);
		}
	}

	public Collection<org.osgi.service.remoteserviceadmin.EndpointDescription> getEndpointDescriptions() {
		synchronized (listenerLock) {
			Collection<org.osgi.service.remoteserviceadmin.EndpointDescription> result = new ArrayList<org.osgi.service.remoteserviceadmin.EndpointDescription>();
			result.addAll(discoveredEndpointDescriptions);
			return result;
		}
	}

	private void logWarning(String message) {
		// XXX todo
		System.out.println(message);
	}

	private void logError(String message) {
		logError(message, null);
	}

	private void logError(String message, Throwable t) {
		Activator a = Activator.getDefault();
		if (a != null) {
			a.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, message, t));
		}
	}

	private DiscoveredEndpointDescription getDiscoveredEndpointDescription(
			IServiceID serviceId, IServiceInfo serviceInfo, boolean discovered) {
		// Get IEndpointDescriptionFactory
		IDiscoveredEndpointDescriptionFactory factory = discovery
				.getDiscoveredEndpointDescriptionFactory();
		if (factory == null) {
			logError("No IEndpointDescriptionFactory found, could not create EndpointDescription for "
					+ (discovered ? "discovered" : "undiscovered")
					+ " serviceInfo=" + serviceInfo);
			return null;
		}
		try {
			// Else get endpoint description factory to create
			// EndpointDescription
			// for given serviceID and serviceInfo
			return (discovered) ? factory.createDiscoveredEndpointDescription(
					locator, serviceInfo) : factory
					.getUndiscoveredEndpointDescription(locator, serviceId);
		} catch (Exception e) {
			logError("Exception calling IEndpointDescriptionFactory."
					+ ((discovered) ? "createDiscoveredEndpointDescription"
							: "getUndiscoveredEndpointDescription"), e);
			return null;
		} catch (NoClassDefFoundError e) {
			logError(
					"NoClassDefFoundError calling IEndpointDescriptionFactory."
							+ ((discovered) ? "createDiscoveredEndpointDescription"
									: "getUndiscoveredEndpointDescription"), e);
			return null;
		}
	}

	public void close() {
		locator = null;
		discovery = null;
		discoveredEndpointDescriptions.clear();
	}
}