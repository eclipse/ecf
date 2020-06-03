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

package org.eclipse.ecf.presence.ui.chatroom;

import org.eclipse.ecf.presence.chatroom.IChatRoomContainer;

/**
 * 
 */
public interface IChatRoomCommandListener {

	/**
	 * Detect and handle input commands. If this handler successfully handles
	 * the line as a command and does not expect the line to be passed on as
	 * chat input, it should return <code>null</code>. If it does not process
	 * the line as a command, and the inputLine should be sent along as chat, it
	 * should return a non-<code>null</code> String.
	 * 
	 * @param chatRoomContainer
	 *            the IChatRoomContainer instance that is to receive the input
	 *            line. If <code>null</code> then the input line is intended
	 *            for the IChatRoomManager itself.
	 * @param inputLine
	 *            the input line holding the prospective command.
	 * @return <code>null</code> if the command has been processed and should
	 *         <b>not</b> be sent along as chat. Non-<code>null</code> if
	 *         the inputLine should be forwarded on as chat.
	 */
	public String handleCommand(IChatRoomContainer chatRoomContainer,
			String inputLine);

}
