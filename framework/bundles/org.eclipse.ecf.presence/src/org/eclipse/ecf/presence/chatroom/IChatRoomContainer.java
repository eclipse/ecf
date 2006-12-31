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
package org.eclipse.ecf.presence.chatroom;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.presence.IIMMessageListener;

/**
 * Container for chat rooms.
 */
public interface IChatRoomContainer extends IContainer {

	/**
	 * Add message listener.
	 * 
	 * @param listener
	 *            the listener to add. Must not be <code>null</code>.
	 */
	public void addMessageListener(IIMMessageListener listener);

	/**
	 * Remove message listener.
	 * 
	 * @param listener
	 *            the listener to remove. Must not be <code>null</code>.
	 */
	public void removeMessageListener(IIMMessageListener listener);

	/**
	 * Get interface for sending messages
	 * 
	 * @return IChatRoomMessageSender. Will be <code>null</code> if no message
	 *         sender available
	 */
	public IChatRoomMessageSender getChatRoomMessageSender();

	/**
	 * Add chat room participant listener. The given listener will be notified
	 * if/when participants are added or removed from given room
	 * 
	 * @param participantListener
	 *            to add. Must not be <code>null</code>.
	 */
	public void addChatRoomParticipantListener(
			IChatRoomParticipantListener participantListener);

	/**
	 * Remove chat room participant listener.
	 * 
	 * @param participantListener
	 *            the participant listener to remove. Must not be
	 *            <code>null</code>.
	 */
	public void removeChatRoomParticipantListener(
			IChatRoomParticipantListener participantListener);
}
