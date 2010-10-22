package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import org.eclipse.ecf.discovery.IServiceInfo;

public interface IServiceInfoFactory {

	public IServiceInfo createServiceInfoForDiscovery(EndpointDescription endpointDescription);
	
	public IServiceInfo createServiceInfoForUndiscovery(EndpointDescription endpointDescription);
	
}
