/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.DiscoveryImpl;
import org.osgi.service.remoteserviceadmin.EndpointDescription;

public class DefaultEndpointDescriptionAdvertiser implements
		IEndpointDescriptionAdvertiser {

	private DiscoveryImpl discovery;

	public DefaultEndpointDescriptionAdvertiser(DiscoveryImpl discovery) {
		this.discovery = discovery;
	}

	public boolean advertise(EndpointDescription endpointDescription) {
		if (endpointDescription == null)
			return false;
		if (endpointDescription instanceof org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription) {
			org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription eed = (org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription) endpointDescription;
			// First get serviceInfoFactory
			IServiceInfoFactory serviceInfoFactory = getServiceInfoFactory();
			if (serviceInfoFactory == null) {
				logError("No IServiceInfoFactory is available.  Cannot publish endpointDescription="
						+ eed);
				return false;
			}
			IDiscoveryAdvertiser[] discoveryAdvertisers = getDiscoveryAdvertisers();
			if (discoveryAdvertisers == null
					|| discoveryAdvertisers.length == 0) {
				logError("No discovery advertisers available.  Cannot publish endpointDescription="
						+ eed);
				return false;
			}
			for (int i = 0; i < discoveryAdvertisers.length; i++) {
				IServiceInfo serviceInfo = serviceInfoFactory
						.createServiceInfoForDiscovery(discoveryAdvertisers[i],
								eed);
				if (serviceInfo == null) {
					logError("Service Info is null.  Cannot publish endpointDescription="
							+ eed);
					continue;
				}
				// Now actually register with advertiser
				return doPublish(discoveryAdvertisers[i], serviceInfo);
			}
		} else {
			// logWarning
			logWarning("publish endpointDescription="
					+ endpointDescription
					+ " is not of ECFEndpointDescription type.  Not publishing.");
		}
		return false;
	}

	protected boolean doPublish(IDiscoveryAdvertiser discoveryAdvertiser,
			IServiceInfo serviceInfo) {
		try {
			discoveryAdvertiser.registerService(serviceInfo);
			return true;
		} catch (Exception e) {
			logError("Exception calling registerService with serviceInfo="
					+ serviceInfo + " for discoveryAdvertiser="
					+ discoveryAdvertiser, e);
			return false;
		}
	}

	protected boolean doUnpublish(IDiscoveryAdvertiser discoveryAdvertiser,
			IServiceInfo serviceInfo) {
		try {
			discoveryAdvertiser.unregisterService(serviceInfo);
			return true;
		} catch (Exception e) {
			logError("Exception calling unregisterService with serviceInfo="
					+ serviceInfo + " for discoveryAdvertiser="
					+ discoveryAdvertiser, e);
			return false;
		}
	}

	protected IServiceInfoFactory getServiceInfoFactory() {
		return discovery.getServiceInfoFactory();
	}

	protected IDiscoveryAdvertiser[] getDiscoveryAdvertisers() {
		return discovery.getDiscoveryAdvertisers();
	}

	private void logWarning(String string) {
		// TODO Auto-generated method stub

	}

	private void logError(String message) {
		logError(message, null);
	}

	private void logError(String message, Throwable e) {
		// XXX todo
		System.err.println(message);
		if (e != null) {
			e.printStackTrace(System.err);
		}
	}

	public boolean unadvertise(EndpointDescription endpointDescription) {
		if (endpointDescription == null)
			return false;
		if (endpointDescription instanceof org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription) {
			org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription eed = (org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription) endpointDescription;
			// First get serviceInfoFactory
			IServiceInfoFactory serviceInfoFactory = getServiceInfoFactory();
			if (serviceInfoFactory == null) {
				logError("No IServiceInfoFactory is available.  Cannot unpublish endpointDescription="
						+ eed);
				return false;
			}
			IDiscoveryAdvertiser[] discoveryAdvertisers = getDiscoveryAdvertisers();
			if (discoveryAdvertisers == null
					|| discoveryAdvertisers.length == 0) {
				logError("No discovery advertisers available.  Cannot unpublish endpointDescription="
						+ eed);
				return false;
			}
			for (int i = 0; i < discoveryAdvertisers.length; i++) {
				IServiceInfo serviceInfo = serviceInfoFactory
						.removeServiceInfoForUndiscovery(
								discoveryAdvertisers[i], eed);
				if (serviceInfo == null) {
					logError("Service Info is null.  Cannot publish endpointDescription="
							+ eed);
					continue;
				}
				// Now actually unregister with advertiser
				return doUnpublish(discoveryAdvertisers[i], serviceInfo);
			}
		} else {
			// logWarning
			logWarning("publish endpointDescription="
					+ endpointDescription
					+ " is not of ECFEndpointDescription type.  Not publishing.");
		}
		return false;
	}

	public void close() {
	}

}
