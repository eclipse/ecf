/*******************************************************************************
* Copyright (c) 2016 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.remoteservice;

import java.util.Map;
import org.eclipse.ecf.remoteservice.RSARemoteServiceContainerAdapter.RSARemoteServiceRegistration;

/**
 * Interface for Remote Service Admin container adapters.   Defines API for registering 
 * and unregistering RSA endpoints.  See RSARemoteServiceContainerAdapter for implementation.
 * @since 8.9
 */
public interface IRSAHostContainerAdapter {

	/**
	 * Register an endpoint via some distribution system.  Implementers
	 * should implement this method to register an endpoint.   Optionally return a Map<String, Object>
	 * that will be used to override properties in the RSA EndpointDescription.
	 * 
	 * @param registration containing information about service to be registered as an endpoint.  Will not
	 * be <code>null</code>.
	 * 
	 * @return Map<String,Object> containing properties and values.  If non-null, any properties
	 * present will be added to the RSA Endpoint Description.   If the properties have ECF-standardized
	 * values (e.g. ecf.endpoint.id) the standardized values will be overridden in the endpoint
	 * description to have the values provided by this Map.  May be null, if no properties are to
	 * override or be added to the RSA endpoint description.
	 */
	Map<String, Object> registerEndpoint(RSARemoteServiceRegistration registration);

	/**
	 * Unregister an endpoint via distribution system used to register as per registerEndpoint 
	 * method above.  After this method completes, any/all resources allocated by registerEndpoint
	 * should be released. 
	 * 
	 * @param registration RSARemoteServiceRegistration previously registered via registerEndpoint.
	 * Will not be <code>null</code>.
	 */
	void unregisterEndpoint(RSARemoteServiceRegistration registration);

}
