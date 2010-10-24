package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import org.eclipse.ecf.discovery.IDiscoveryLocator;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.identity.IServiceID;

public abstract class AbstractEndpointDescriptionFactory extends AbstractMetadataFactory implements IEndpointDescriptionFactory {

	public EndpointDescription createDiscoveredEndpointDescription(IDiscoveryLocator locator,
			IServiceInfo discoveredServiceInfo) {
		// XXX todo
		return null;
	}

	public EndpointDescription getUndiscoveredEndpointDescription(IDiscoveryLocator locator,
			IServiceID serviceID) {
		// XXX todo
		return null;
	}

}
