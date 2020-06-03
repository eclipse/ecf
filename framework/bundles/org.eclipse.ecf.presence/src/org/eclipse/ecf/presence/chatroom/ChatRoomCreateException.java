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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.util.ECFException;

/**
 * Exception class thrown by
 * {@link IChatRoomManager#createChatRoom(String, java.util.Map)}
 */
public class ChatRoomCreateException extends ECFException {

	private static final long serialVersionUID = -2605728854430323369L;

	protected String roomname;

	public ChatRoomCreateException() {
		// null constructor
	}

	public String getRoomName() {
		return roomname;
	}

	/**
	 * @param roomname
	 * @param message
	 * @param cause
	 */
	public ChatRoomCreateException(String roomname, String message, Throwable cause) {
		super(message, cause);
		this.roomname = roomname;
	}

	/**
	 * @param roomname
	 * @param message
	 */
	public ChatRoomCreateException(String roomname, String message) {
		this(roomname, message, null);
	}

	/**
	 * @param roomname
	 * @param cause
	 */
	public ChatRoomCreateException(String roomname, Throwable cause) {
		this(roomname, null, cause);
	}

	/**
	 * @param status
	 */
	public ChatRoomCreateException(IStatus status) {
		super(status);
	}

}
