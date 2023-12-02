/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.core.security;

import java.security.PermissionCollection;
import org.eclipse.ecf.core.identity.ID;

/**
 * Connect policy typically implemented by servers
 */
public interface IConnectHandlerPolicy extends IContainerPolicy {
	/**
	 * Check connect request
	 * 
	 * @param address
	 *            the address for the remote client
	 * @param fromID
	 *            the ID of the container making the connect request
	 * @param targetID
	 *            the ID of the container responding to that connect request
	 * @param targetGroup
	 *            the target name of the group that is being connected to
	 * @param connectData
	 *            arbitrary data associated with the join request
	 * @return PermissionCollection a collection of permissions associated with
	 *         a successful acceptance of join request
	 * @throws Exception thrown if connect should not be allowed
	 */
	public PermissionCollection checkConnect(Object address, ID fromID, ID targetID, String targetGroup, Object connectData) throws Exception;
}
