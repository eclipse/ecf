package org.eclipse.ecf.remoteservice.client;

import org.eclipse.ecf.remoteservice.IRemoteService;

/**
 * @since 8.12
 */
public interface IRemoteServiceFactory {

	IRemoteService createRemoteService(RemoteServiceClientRegistration registration);

}
