package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.IServiceProperties;
import org.eclipse.ecf.discovery.ServiceInfo;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.discovery.identity.ServiceIDFactory;

public abstract class AbstractServiceInfoFactory extends
		AbstractMetadataFactory implements IServiceInfoFactory {

	protected Map<IServiceID, IServiceInfo> serviceInfos = new HashMap();

	protected boolean addServiceInfo(IServiceInfo serviceInfo) {
		IServiceID sid = serviceInfo.getServiceID();
		synchronized (serviceInfos) {
			if (!hasServiceInfo(sid)) {
				serviceInfos.put(sid, serviceInfo);
				return true;
			} else return false;
		}
	}
	
	protected IServiceInfo removeServiceInfo(IServiceInfo serviceInfo) {
		synchronized (serviceInfos) {
			return serviceInfos.remove(serviceInfo.getServiceID());
		}
	}
	
	protected boolean hasServiceInfo(IServiceID serviceID) {
		synchronized (serviceInfos) {
			return serviceInfos.containsKey(serviceID);
		}
	}
	
	public IServiceInfo createServiceInfoForDiscovery(
			EndpointDescription endpointDescription,  IDiscoveryAdvertiser advertiser) {
		
		IServiceTypeID serviceTypeID = createServiceTypeID(endpointDescription,advertiser);
		String serviceName = createServiceName(endpointDescription,advertiser,serviceTypeID);
		URI uri = createURI(endpointDescription,advertiser,serviceTypeID,serviceName);
		IServiceProperties serviceProperties = createServiceProperties(endpointDescription,advertiser,serviceTypeID,serviceName,uri);
		
		IServiceInfo serviceInfo = new ServiceInfo(uri,serviceName,serviceTypeID,serviceProperties);
		return addServiceInfo(serviceInfo)?serviceInfo:null;
	}

	protected IServiceProperties createServiceProperties(
			EndpointDescription endpointDescription,
			IDiscoveryAdvertiser advertiser, IServiceTypeID serviceTypeID,
			String serviceName, URI uri) {
		// TODO Auto-generated method stub
		return null;
	}

	protected URI createURI(EndpointDescription endpointDescription,
			IDiscoveryAdvertiser advertiser, IServiceTypeID serviceTypeID,
			String serviceName) {
		// TODO Auto-generated method stub
		return null;
	}

	protected String createServiceName(EndpointDescription endpointDescription,
			IDiscoveryAdvertiser advertiser, IServiceTypeID serviceTypeID) {
		// TODO Auto-generated method stub
		return null;
	}

	protected URI createURI(EndpointDescription endpointDescription,IDiscoveryAdvertiser advertiser) {
		// TODO Auto-generated method stub
		return null;
	}

	protected IServiceTypeID createServiceTypeID(
			EndpointDescription endpointDescription, IDiscoveryAdvertiser advertiser) {
		Map props = endpointDescription.getProperties();
		String[] scopes = getStringArrayPropertyWithDefault(props, RemoteConstants.DISCOVERY_SCOPE, IServiceTypeID.DEFAULT_SCOPE);
		String[] protocols = getStringArrayPropertyWithDefault(props, RemoteConstants.DISCOVERY_PROTOCOLS, IServiceTypeID.DEFAULT_SCOPE);
		String namingAuthority = getStringPropertyWithDefault(props, RemoteConstants.DISCOVERY_NAMING_AUTHORITY, IServiceTypeID.DEFAULT_NA);
		return ServiceIDFactory.getDefault().createServiceTypeID(advertiser.getServicesNamespace(),
				new String[] { RemoteConstants.SERVICE_TYPE }, scopes,
				protocols, namingAuthority);
	}

	public IServiceInfo createServiceInfoForUndiscovery(
			EndpointDescription endpointDescription,  IDiscoveryAdvertiser advertiser) {
		// XXX todo
		return null;
	}

	public void close() {
		synchronized (serviceInfos) {
			serviceInfos.clear();
		}
		super.close();
	}
}
