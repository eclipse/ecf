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

package org.eclipse.ecf.presence.im;

/**
 * Chat manager
 */
public interface IChatManager {

	/**
	 * Add chat message listener.
	 * 
	 * @param listener the listener to add.  Must not be null.
	 */
	public void addChatMessageListener(IChatMessageListener listener);

	/**
	 * Remove chat message listener.
	 * 
	 * @param listener the listener to remove.  Must not be null.
	 */
	public void removeChatMessageListener(IChatMessageListener listener);

	/**
	 * Get chat message sender.  If sending chat messages not supported
	 * by this provider then null will be returned.
	 * 
	 * @return IChatMessageSender to use for sending chat message.  If
	 * null, sending chat messages not supported by this provider.
	 */
	public IChatMessageSender getChatMessageSender();

}
