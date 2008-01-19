/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.discovery;

import java.net.URI;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ecf.discovery.identity.IServiceID;

/**
 * Service information contract. Defines the information associated with a
 * remotely discoverable service
 * 
 */
public interface IServiceInfo extends IAdaptable {
	/**
	 * Get URI for service
	 * 
	 * @return {@link java.net.URI} the location for the service. May be <code>null</code> if location
	 *         is not known.
	 */
	public URI getLocation();

	/**
	 * Get ServiceID for service.
	 * 
	 * @return ServiceID the serviceID for the service. Will not be <code>null</code>.
	 */
	public IServiceID getServiceID();

	/**
	 * The priority for the service
	 * 
	 * @return int the priority. Zero if no priority information for service.
	 */
	public int getPriority();

	/**
	 * The weight for the service. Zero if no weight information for service.
	 * 
	 * @return int the weight
	 */
	public int getWeight();

	/**
	 * Map with any/all properties associated with the service. Properties are
	 * assumed to be name/value pairs, both of type String.
	 * 
	 * @return Map the properties associated with this service.  Will not be <code>null</code>.
	 */
	public IServiceProperties getServiceProperties();
}
