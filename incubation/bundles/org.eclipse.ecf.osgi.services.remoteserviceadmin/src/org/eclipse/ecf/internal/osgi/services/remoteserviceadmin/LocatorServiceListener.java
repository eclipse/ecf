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

import java.util.Arrays;

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
import org.eclipse.osgi.framework.eventmgr.ListenerQueue;
import org.osgi.service.remoteserviceadmin.EndpointListener;

class LocatorServiceListener implements IServiceListener {

	private Object listenerLock = new Object();

	private ListenerQueue queue;
	private IDiscoveryLocator locator;

	class EndpointListenerEvent {

		private EndpointListener endpointListener;
		private org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription;
		private String matchingFilter;
		private boolean discovered;

		public EndpointListenerEvent(
				EndpointListener endpointListener,
				org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription,
				String matchingFilter, boolean discovered) {
			this.endpointListener = endpointListener;
			this.endpointDescription = endpointDescription;
			this.matchingFilter = matchingFilter;
			this.discovered = discovered;
		}

		public EndpointListener getEndpointListener() {
			return endpointListener;
		}

		public org.osgi.service.remoteserviceadmin.EndpointDescription getEndointDescription() {
			return endpointDescription;
		}

		public String getMatchingFilter() {
			return matchingFilter;
		}

		public boolean isDiscovered() {
			return discovered;
		}
	}

	public LocatorServiceListener(ListenerQueue queue) {
		this(null, queue);
	}

	public LocatorServiceListener(IDiscoveryLocator locator, ListenerQueue queue) {
		this.locator = locator;
		this.queue = queue;

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
			Activator.EndpointListenerHolder[] endpointListenerHolders = Activator
					.getDefault().getMatchingEndpointListenerHolders(
							endpointDescription);
			if (endpointListenerHolders != null) {
				for (int i = 0; i < endpointListenerHolders.length; i++) {
					queue.dispatchEventAsynchronous(
							0,
							new EndpointListenerEvent(
									endpointListenerHolders[i].getListener(),
									endpointListenerHolders[i].getDescription(),
									endpointListenerHolders[i]
											.getMatchingFilter(), discovered));
				}
			} else {
				logWarning("No matching EndpointListeners found for "
						+ (discovered ? "discovered" : "undiscovered")
						+ " endpointDescription=" + endpointDescription);
			}
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
		// Get activator
		Activator activator = Activator.getDefault();
		if (activator == null)
			return null;
		// Get IEndpointDescriptionFactory
		IDiscoveredEndpointDescriptionFactory factory = activator
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
		if (locator != null) {
			locator = null;
		}
		if (queue != null) {
			queue = null;
		}
	}
}