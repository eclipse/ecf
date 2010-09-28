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

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.discovery.IServiceEvent;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.IServiceListener;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.Activator.EndpointListenerHolder;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.IEndpointDescriptionFactory;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.EndpointListener;

class LocatorServiceListener implements IServiceListener {

	public void serviceDiscovered(IServiceEvent anEvent) {
		IServiceInfo serviceInfo = anEvent.getServiceInfo();
		IServiceID serviceID = serviceInfo.getServiceID();
		if (matchServiceID(serviceID)) {
			handleOSGiServiceEndpoint(serviceID, serviceInfo, true);
		}
	}

	public void serviceUndiscovered(IServiceEvent anEvent) {
		IServiceInfo serviceInfo = anEvent.getServiceInfo();
		IServiceID serviceID = serviceInfo.getServiceID();
		if (matchServiceID(serviceID)) {
			handleOSGiServiceEndpoint(serviceID, serviceInfo, false);
		}
	}

	private void handleOSGiServiceEndpoint(IServiceID serviceId,
			IServiceInfo serviceInfo, boolean discovered) {
		EndpointDescription description = createEndpointDescription(serviceId,
				serviceInfo, discovered);
		if (description != null) {
			Activator.EndpointListenerHolder[] endpointListenerHolders = Activator
					.getDefault().getMatchingEndpointListenerHolders(
							description);
			if (endpointListenerHolders != null) {
				for (int i = 0; i < endpointListenerHolders.length; i++) {
					notifyEndpointListener(endpointListenerHolders[i],
							discovered);
				}
			} else {
				// XXX log?
			}
		}
	}

	private void notifyEndpointListener(
			final EndpointListenerHolder endpointListenerHolder,
			final boolean discovered) {
		// XXX should this notification be done in separate thread?
		SafeRunner.run(new ISafeRunnable() {
			public void handleException(Throwable exception) {
				Activator a = Activator.getDefault();
				if (a != null)
					a.log(new Status(
							IStatus.ERROR,
							Activator.PLUGIN_ID,
							IStatus.ERROR,
							"notifyEndpointListener: Exception in EndpointListener", exception)); //$NON-NLS-1$
			}

			public void run() throws Exception {
				EndpointListener l = endpointListenerHolder.getListener();
				EndpointDescription endpoint = endpointListenerHolder
						.getDescription();
				String matchedFilter = endpointListenerHolder.getFilter();
				// Call endpointAdded or endpointRemoved
				if (discovered)
					l.endpointAdded(endpoint, matchedFilter);
				else
					l.endpointRemoved(endpoint, matchedFilter);
			}
		});
	}

	private EndpointDescription createEndpointDescription(IServiceID serviceId,
			IServiceInfo serviceInfo, boolean discovered) {
		// Get activator
		Activator activator = Activator.getDefault();
		if (activator == null)
			return null;
		// Get IEndpointDescriptionFactory
		IEndpointDescriptionFactory factory = activator
				.getEndpointDescriptionFactory();
		if (factory == null) {
			activator
					.log(new Status(
							IStatus.ERROR,
							Activator.PLUGIN_ID,
							"No IEndpointDescriptionFactory found, could not create EndpointDescription for discovered service"));
			return null;
		}
		return (discovered) ? factory.createDiscoveredEndpointDescription(
				serviceId, serviceInfo) : factory
				.createUndiscoveredEndpointDescription(serviceId, serviceInfo);
	}

	private boolean matchServiceID(IServiceID serviceId) {
		if (Arrays.asList(serviceId.getServiceTypeID().getServices()).contains(
				"osgiservices"))
			return true;
		return false;
	}

}