package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.identity.IServiceID;

public abstract class AbstractEndpointDescriptionFactory extends AbstractMetadataFactory implements IEndpointDescriptionFactory {

	public EndpointDescription createDiscoveredEndpointDescription(
			IServiceInfo discoveredServiceInfo) {
		return null;
	}

	public EndpointDescription getUndiscoveredEndpointDescription(
			IServiceID serviceID) {
		return null;
	}

}
