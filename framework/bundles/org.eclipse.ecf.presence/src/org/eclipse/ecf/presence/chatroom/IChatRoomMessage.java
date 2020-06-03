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

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.presence.IIMMessage;

/**
 * Chat room message.
 */
public interface IChatRoomMessage extends IIMMessage {

	/** 
	 * Get the room ID for the room of this message.
	 * 
	 * @return ID of chat room associated with this message.
	 */
	public ID getChatRoomID();
	
	/**
	 * Get the actual message sent to the chat room
	 * 
	 * @return String message sent to chat room. Will not be <code>null</code>.
	 */
	public String getMessage();

}
