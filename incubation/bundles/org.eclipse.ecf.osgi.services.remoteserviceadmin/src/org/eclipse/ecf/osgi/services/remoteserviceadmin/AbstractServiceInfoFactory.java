package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import org.eclipse.ecf.discovery.IServiceInfo;

public class AbstractServiceInfoFactory extends AbstractMetadataFactory implements IServiceInfoFactory {

	public IServiceInfo createServiceInfoForDiscovery(
			EndpointDescription endpointDescription) {
		// XXX todo
		return null;
	}

	public IServiceInfo createServiceInfoForUndiscovery(
			EndpointDescription endpointDescription) {
		// XXX todo
		return null;
	}

}
