/*******************************************************************************
 * Copyright (c) 2007 Versant Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.discovery.identity;

import org.eclipse.ecf.core.identity.ID;

/**
 * Service type ID contract.  
 * 
 */
public interface IServiceTypeID extends ID {
	/*
	 * jSLP => getServices()[0]:getServices()[1][.getNamingAuthoriy():getService()[n]
	 * jmDNS => _getServices()[0]._getServices()[n]._getProtocol()[0]._getScopes()[0]
	 */
	/*
	 * jSLP => naming authority (IANA or custom)
	 * jmDNS => IANA
	 */
	/**
	 * @return String Naming Authority for this ServiceType.  May be <code>null</code>.
	 */
	public String getNamingAuthority();

	/*
	 * jSLP => unknown (0) only known at the service consumer level
	 * jmDNS => protocols (udp/ip or tcp/ip or both) (1)
	 */
	/**
	 * @return String[] of protocols supported.  Will not be <code>null</code>, but may
	 * be empty array.
	 */
	public String[] getProtocols();

	/*
	 * jSLP => Scopes (n)
	 * jmDNS => domain (1)
	 */
	/**
	 * @return The scopes in which this Service is registered.  Will not be <code>null</code>, but may
	 * be empty array.
	 */
	public String[] getScopes();

	/*
	 * jSLP => abstract and concrete types (n)
	 * jmDNS => everything before port (n)
	 */

	/**
	 * @return The name of the Service, if the underlying discovery mechanism
	 *         supports naming hierarchies, the hierarchy will be returned
	 *         flattened as an array.  Will not be <code>null</code>, but may
	 *         be empty array.
	 */
	public String[] getServices();

}
