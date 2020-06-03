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
package org.eclipse.ecf.core.sharedobject;

import org.eclipse.ecf.core.security.IConnectInitiatorPolicy;

/**
 * Interface for shared object containers that are clients rather than group
 * manager
 * 
 * @see ISharedObjectContainerGroupManager
 */
public interface ISharedObjectContainerClient {

	/**
	 * Set the connect initiator policy handler for authentication policy
	 * 
	 * @param policy
	 *            the policy to use
	 */
	public void setConnectInitiatorPolicy(IConnectInitiatorPolicy policy);
}
