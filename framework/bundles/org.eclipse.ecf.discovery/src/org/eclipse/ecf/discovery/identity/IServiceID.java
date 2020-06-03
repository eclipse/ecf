/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.discovery.identity;

import java.net.URI;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.discovery.IServiceInfo;

/**
 * Service identity contract.
 */
public interface IServiceID extends ID {

	/**
	 * Get service type ID for this ID.
	 * 
	 * @return IServiceTypeID. Will not be <code>null</code>.
	 */
	public IServiceTypeID getServiceTypeID();

	/**
	 * Get service name for this ID.
	 * 
	 * @return String service name. May be <code>null</code>.
	 * @deprecated Use {@link IServiceInfo#getServiceName()} instead
	 */
	public String getServiceName();

	/**
	 * @return URI the location for this serviceID
	 * @since 3.0
	 */
	public URI getLocation();
}
