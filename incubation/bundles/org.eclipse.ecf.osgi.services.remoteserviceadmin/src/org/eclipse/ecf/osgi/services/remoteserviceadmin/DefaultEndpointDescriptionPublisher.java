package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.Activator;
import org.osgi.service.remoteserviceadmin.EndpointDescription;

public class DefaultEndpointDescriptionPublisher implements
		IEndpointDescriptionPublisher {

	public boolean publish(EndpointDescription endpointDescription) {
		boolean published = false;
		if (endpointDescription == null)
			return published;
		if (endpointDescription instanceof org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription) {
			org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription eed = (org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription) endpointDescription;
			// First get serviceInfoFactory
			IServiceInfoFactory serviceInfoFactory = Activator.getDefault()
					.getServiceInfoFactory();
			if (serviceInfoFactory == null) {
				logError("No IServiceInfoFactory is available.  Cannot publish endpointDescription="
						+ eed);
				return published;
			}
			IDiscoveryAdvertiser[] discoveryAdvertisers = Activator
					.getDefault().getDiscoveryAdvertisers();
			if (discoveryAdvertisers == null
					|| discoveryAdvertisers.length == 0) {
				logError("No discovery advertisers available.  Cannot publish endpointDescription="
						+ eed);
				return published;
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
				discoveryAdvertisers[i].registerService(serviceInfo);
				published = true;
			}
		} else {
			// logWarning
			logWarning("publish endpointDescription="
					+ endpointDescription
					+ " is not of ECFEndpointDescription type.  Not publishing.");
		}
		return published;
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

	public boolean unpublish(EndpointDescription endpointDescription) {
		boolean unpublished = false;
		if (endpointDescription == null)
			return unpublished;
		if (endpointDescription instanceof org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription) {
			org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription eed = (org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription) endpointDescription;
			// First get serviceInfoFactory
			IServiceInfoFactory serviceInfoFactory = Activator.getDefault()
					.getServiceInfoFactory();
			if (serviceInfoFactory == null) {
				logError("No IServiceInfoFactory is available.  Cannot unpublish endpointDescription="
						+ eed);
				return unpublished;
			}
			IDiscoveryAdvertiser[] discoveryAdvertisers = Activator
					.getDefault().getDiscoveryAdvertisers();
			if (discoveryAdvertisers == null
					|| discoveryAdvertisers.length == 0) {
				logError("No discovery advertisers available.  Cannot unpublish endpointDescription="
						+ eed);
				return unpublished;
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
				discoveryAdvertisers[i].unregisterService(serviceInfo);
				unpublished = true;
			}
		} else {
			// logWarning
			logWarning("publish endpointDescription="
					+ endpointDescription
					+ " is not of ECFEndpointDescription type.  Not publishing.");
		}
		return unpublished;
	}

	public void close() {
	}

}
