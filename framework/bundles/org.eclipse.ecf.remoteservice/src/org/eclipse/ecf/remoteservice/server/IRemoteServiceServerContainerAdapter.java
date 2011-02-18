package org.eclipse.ecf.remoteservice.server;

import java.util.Dictionary;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;

public interface IRemoteServiceServerContainerAdapter extends IRemoteServiceContainerAdapter {

	/**
	 * Register a new remote service. This method is to be called by the service
	 * server...i.e. the client that wishes to make available a service to other
	 * client within this container.
	 * 
	 * @param clazzes
	 *            the interface classes that the service exposes to remote
	 *            clients. Must not be <code>null</code> and must not be an
	 *            empty array.
	 * @param provider the remote service server provider.  Must not be <code>null</code>.
	 * @param properties
	 *            to be associated with service
	 * @return IRemoteServiceRegistration the service registration. Will not
	 *         return <code>null</code> .
	 */
	public IRemoteServiceReference registerRemoteService(String[] clazzes, IRemoteServiceServerProvider provider, Dictionary properties);

}
