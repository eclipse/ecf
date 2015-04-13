package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import org.eclipse.ecf.discovery.identity.IServiceID;

/**
 * @since 4.3
 */
public interface IEndpointDescriptionLocator {

	/**
	 * Get the service ID associated with the 
	 * @param endpointDescription
	 * @return
	 */
	IServiceID getNetworkDiscoveredService(EndpointDescription endpointDescription);
	
	/**
	 * Discover the given endpointDescription.  This method will not block
	 * and will result in local EndpointEventListeners to be notified that the
	 * given endpointDescription is discovered.
	 * about
	 * @param endpointDescription must not be null
	 */
	void discoverEndpoint(EndpointDescription endpointDescription);
	/**
	 * Update the given endpointDescription.  This method will not block
	 * and will result in local EndpointEventListeners to be notified that the
	 * given endpointDescription is updated.
	 * about
	 * @param endpointDescription must not be null
	 */
	void updateEndpoint(EndpointDescription endpointDescription);
	/**
	 * Remove the given endpointDescription.  This method will not block
	 * and will result in local EndpointEventListeners to be notified that the
	 * given endpointDescription is removed.
	 * about
	 * @param endpointDescription must not be null
	 */
	void undiscoverEndpoint(EndpointDescription endpointDescription);
	
}
