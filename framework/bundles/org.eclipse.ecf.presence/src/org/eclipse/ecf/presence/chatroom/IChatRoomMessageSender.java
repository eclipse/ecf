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
package org.eclipse.ecf.presence.chatroom;

import org.eclipse.ecf.core.util.ECFException;

/**
 * Chat message sender. Interface for sending chat messages within an
 * {@link IChatRoomContainer}. Access to instances implementing this interface
 * is provided via the {@link IChatRoomContainer#getChatRoomMessageSender()}
 * 
 * @see IChatRoomManager
 */
public interface IChatRoomMessageSender {
	/**
	 * Send a message to chat room
	 * 
	 * @param message
	 *            the message to send. Must not be <code>null</code>.
	 * @throws ECFException
	 *             thrown if message cannot be sent (e.g. because of previous
	 *             disconnect)
	 */
	public void sendMessage(String message) throws ECFException;
}
