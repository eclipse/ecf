/****************************************************************************
 * Copyright (c) 2009 EclipseSource and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteservice;

import org.eclipse.ecf.core.identity.ID;

/**
 * Remote service ID.
 *
 * @since 3.0
 */
public interface IRemoteServiceID extends ID {
	/**
	 * Get the container ID for this remote service.  Will not return <code>null</code>.
	 * @return ID the ID for the container associated with this remote service.  Will not return <code>null</code>.
	 */
	public ID getContainerID();

	/**
	 * Get container-relative ID for the remote service identified
	 * @return int the container-relative ID.  
	 */
	public long getContainerRelativeID();
}
