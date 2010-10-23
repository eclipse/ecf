package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.eclipse.ecf.discovery.IServiceInfo;

public interface IServiceInfoFactory {

	public IServiceInfo createServiceInfoForDiscovery(EndpointDescription endpointDescription, IDiscoveryAdvertiser advertiser);
	
	public IServiceInfo removeServiceInfoForUndiscovery(EndpointDescription endpointDescription, IDiscoveryAdvertiser advertiser);
	
}
