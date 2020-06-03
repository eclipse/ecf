/****************************************************************************
 * Copyright (c) 2004 Composent, Inc., Peter Nehrer, Boris Bokowski.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.datashare;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;

/**
 * Channel for sending messages
 * 
 */
public interface IChannel extends IAbstractChannel {
	/**
	 * Send message to remote instances of this channel
	 * 
	 * @param message
	 *            the byte [] message to send. Must not be <code>null</code>.
	 * @throws ECFException
	 *             if some problem sending message
	 */
	public void sendMessage(byte[] message) throws ECFException;

	/**
	 * Send message to remote instances of this channel
	 * 
	 * @param receiver
	 *            the ID of the container to receive message. If
	 *            <code>null</code>, message sent to all current members of
	 *            group
	 * @param message
	 *            the byte [] message to send. Must not be <code>null</code>.
	 * @throws ECFException
	 *             if some problem sending message
	 */
	public void sendMessage(ID receiver, byte[] message) throws ECFException;
}
