/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.discovery;

import java.net.InetAddress;
import java.util.Map;

import org.eclipse.ecf.core.identity.ServiceID;
/**
 * Service information.  Defines the information associated
 * with a service
 * 
 * @author slewis
 *
 */
public interface IServiceInfo {
	/**
	 * Get InetAddress for service
	 * @return InetAddress the address for the service.  
	 * May be null if address is not known.
	 */
	public InetAddress getAddress();
	/**
	 * Get ServiceID for service. 
	 * 
	 * @return ServiceID the serviceID for the service.  Must not be null.
	 */
	public ServiceID getServiceID();
	/**
	 * The port for the service
	 * 
	 * @return port
	 */
	public int getPort();
	/**
	 * The priority for the service
	 * 
	 * @return int the priority.  Zero if no priority information for service.
	 */
	public int getPriority();
	/**
	 * The weight for the service.  Zero if no weight information for service.
	 * 
	 * @return int the weight
	 */
	public int getWeight();
	/**
	 * Map with any/all properties associated with the service.
	 * Properties are assumed to be name/value pairs, both of type String.
	 *
	 * @return Map the properties associated with this service
	 */
	public Map getProperties();
}
