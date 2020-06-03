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

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;

/**
 * Policy handler for connect initiator (clients).
 *
 */
public interface IConnectInitiatorPolicy extends IContainerPolicy {

	/**
	 * Create connect data for given IContainer, given targetID and given context
	 * 
	 * @param container the container that is doing the connecting
	 * @param targetID the target ID from {@link IContainer#connect(ID, IConnectContext)}
	 * @param context from {@link IContainer#connect(ID, IConnectContext)}
	 * @return Object that will be used as data for the connect call
	 */
	public Object createConnectData(IContainer container, ID targetID, IConnectContext context);

	/**
	 * Get connect timeout (in ms)
	 * @return int connect timeout in ms
	 */
	public int getConnectTimeout();
}
