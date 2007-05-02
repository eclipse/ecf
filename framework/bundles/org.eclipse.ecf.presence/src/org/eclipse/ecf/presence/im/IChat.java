/****************************************************************************
 * Copyright (c) 20047 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.presence.im;

import java.util.Map;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.presence.IPresenceContainerAdapter;
import org.eclipse.ecf.presence.chatroom.IChatRoomContainer;

/**
 * Object representing a specific two-way chat.
 */
public interface IChat {

	/**
	 * Get the receiver for this chat.
	 * 
	 * @return ID of receiver.  Will not return <code>null</code>.
	 */
	public ID getReceiverID();

	/**
	 * Get the thread ID for this chat.
	 * 
	 * @return ID of this chat thread.  Will not return <code>null</code>.
	 */
	public ID getThreadID();

	/**
	 * Send chat message to given ID.
	 * 
	 * @param type
	 *            the IChatMessage.Type of the message. May not be null.
	 * 
	 * @param subject
	 *            the subject of the message. May be null.
	 * 
	 * @param body
	 *            the body of the message to send. May be null.
	 * 
	 * @param properties
	 *            properties associated with message. May be null.
	 * 
	 * @throws ECFException
	 *             thrown if toID is null, or currently disconnected
	 */
	public void sendChatMessage(IChatMessage.Type type, String subject,
			String body, Map properties) throws ECFException;

	
	/**
	 * Create a new IChatRoomContainer instance. This method can be used to
	 * create to a chat room identified by this two-way chat.  If supported
	 * by the provider, this allows moving from a two-way chat represented
	 * by this IChat instance to an n-way chat room container.
	 * 
	 * @return non-null IChatRoomContainer instance. Will not return
	 *         <code>null</code>.
	 * @throws ContainerCreateException
	 *             if chat room container cannot be made.
	 */
	public IChatRoomContainer createChatRoom() throws ContainerCreateException;
	
	/**
	 * Get presence container adapter for this chat instance.
	 * @return IPresenceContainerAdapter for this chat instance.  Will not
	 * return <code>null</code>.
	 */
	public IPresenceContainerAdapter getPresenceContainerAdapter();
}
