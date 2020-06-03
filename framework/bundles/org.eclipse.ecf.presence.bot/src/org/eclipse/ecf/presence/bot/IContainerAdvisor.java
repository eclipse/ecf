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

package org.eclipse.ecf.presence.bot;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;

/**
 * Advisor instance for receiving container initialization and pre-connect
 * notifications.
 * 
 */
public interface IContainerAdvisor {

	/**
	 * This method will be called prior to calling the container's
	 * {@link IContainer#connect(ID, org.eclipse.ecf.core.security.IConnectContext)}
	 * method.
	 * 
	 * @param container
	 *            the container instance created. Will not be <code>null</code>.
	 * @param targetID
	 *            the target id instance to connect to. Will not be
	 *            <code>null</code>.
	 */
	public void preContainerConnect(IContainer container, ID targetID);

}
