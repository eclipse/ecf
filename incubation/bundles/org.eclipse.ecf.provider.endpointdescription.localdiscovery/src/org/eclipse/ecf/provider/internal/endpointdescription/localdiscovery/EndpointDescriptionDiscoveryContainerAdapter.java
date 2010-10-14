package org.eclipse.ecf.provider.internal.endpointdescription.localdiscovery;

import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.discovery.AbstractDiscoveryContainerAdapter;
import org.eclipse.ecf.discovery.DiscoveryContainerConfig;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;

public class EndpointDescriptionDiscoveryContainerAdapter extends
		AbstractDiscoveryContainerAdapter {

	public EndpointDescriptionDiscoveryContainerAdapter(String aNamespaceName,
			DiscoveryContainerConfig aConfig) {
		super(aNamespaceName, aConfig);
		// TODO Auto-generated constructor stub
	}

	public IServiceInfo getServiceInfo(IServiceID aServiceID) {
		// TODO Auto-generated method stub
		return null;
	}

	public IServiceInfo[] getServices() {
		// TODO Auto-generated method stub
		return null;
	}

	public IServiceInfo[] getServices(IServiceTypeID aServiceTypeID) {
		// TODO Auto-generated method stub
		return null;
	}

	public IServiceTypeID[] getServiceTypes() {
		// TODO Auto-generated method stub
		return null;
	}
	public void registerService(IServiceInfo serviceInfo) {
		// TODO Auto-generated method stub

	}

	public void unregisterService(IServiceInfo serviceInfo) {
		// TODO Auto-generated method stub

	}

	public void connect(ID targetID, IConnectContext connectContext)
			throws ContainerConnectException {
		// Do nothing...no connection
	}

	public ID getConnectedID() {
		return getID();
	}

	public void disconnect() {
		// No disconnection
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		// No adapters supported
		return null;
	}

}
