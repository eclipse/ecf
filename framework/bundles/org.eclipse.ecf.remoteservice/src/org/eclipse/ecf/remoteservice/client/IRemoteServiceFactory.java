package org.eclipse.ecf.remoteservice.client;

import org.eclipse.ecf.remoteservice.IRemoteService;

/**
 * Create a remote service instance for a given RemoteServiceClientRegistration.
 * 
 * @since 8.12
 */
public interface IRemoteServiceFactory {

	IRemoteService createRemoteService(RemoteServiceClientRegistration registration);

}
