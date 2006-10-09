/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.presence.chat;

import java.io.IOException;

/**
 * Chat message sender. Interface for sending chat messages within an
 * {@link IChatRoomContainer}
 * 
 */
public interface IChatMessageSender {
	/**
	 * Send a message to chat room
	 * 
	 * @param message
	 *            the message to send
	 * @throws IOException
	 *             thrown if message cannot be sent
	 */
	public void sendMessage(String message) throws IOException;
}
