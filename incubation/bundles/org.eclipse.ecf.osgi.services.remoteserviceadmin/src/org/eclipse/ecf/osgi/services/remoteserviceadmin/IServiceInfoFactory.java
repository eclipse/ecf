package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.eclipse.ecf.discovery.IServiceInfo;

public interface IServiceInfoFactory {

	public IServiceInfo createServiceInfoForDiscovery(IDiscoveryAdvertiser advertiser, EndpointDescription endpointDescription);
	
	public IServiceInfo removeServiceInfoForUndiscovery(IDiscoveryAdvertiser advertiser, EndpointDescription endpointDescription);
	
}
