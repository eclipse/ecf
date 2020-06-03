/****************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.presence.bot;

import org.eclipse.ecf.presence.chatroom.IChatRoomMessage;

/**
 * Message handler for receiving a chat room message.
 */
public interface IChatRoomMessageHandler extends IChatRoomContainerAdvisor {

	/**
	 * Initialize robot with robot entry data.
	 * 
	 * @param robot
	 *            the robot to initialize. Will not be <code>null</code>.
	 */
	public void init(IChatRoomBotEntry robot);

	/**
	 * This method is called when a {@link IChatRoomMessage} is received.
	 * 
	 * @param message
	 *            the {@link IChatRoomMessage} received. Will not be
	 *            <code>null</code>. Implementers should not block the
	 *            calling thread. Any methods on the given <code>message</code>
	 *            parameter may be called.
	 */
	public void handleRoomMessage(IChatRoomMessage message);

}
