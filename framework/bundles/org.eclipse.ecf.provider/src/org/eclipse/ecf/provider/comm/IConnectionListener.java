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
package org.eclipse.ecf.provider.comm;

import org.eclipse.ecf.core.identity.ID;

/**
 * Connection listener
 * 
 * @see IConnection#addListener(IConnectionListener)
 * 
 */
public interface IConnectionListener {
	/**
	 * Get ID of event handler
	 * 
	 * @return ID of event handler
	 */
	public ID getEventHandlerID();

	/**
	 * Handle disconnect event
	 * 
	 * @param event
	 *            the disconnect event
	 */
	public void handleDisconnectEvent(DisconnectEvent event);

	/**
	 * Handle connect event
	 * 
	 * @param event the connection event.
	 */
	public void handleConnectEvent(ConnectionEvent event);

}