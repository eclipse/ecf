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

import org.eclipse.ecf.presence.IIMMessageListener;
import org.eclipse.ecf.presence.history.IHistoryManager;

/**
 * Chat manager access entry interface. The chat manager supports the sending
 * and receiving of person-to-person messages for a given account.
 */
public interface IChatManager {

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
	 * Get chat message sender. If sending chat messages not supported by this
	 * provider then <code>null</code> will be returned.
	 * 
	 * @return IChatMessageSender to use for sending chat message. If
	 *         <code>null</code>, sending chat messages not supported by this
	 *         provider.
	 */
	public IChatMessageSender getChatMessageSender();

	/**
	 * Get typing message sender. If sending typing messages not supported by
	 * this provider then <code>null</code> will be returned.
	 * 
	 * @return ITypingMessageSender to use for sending typing messages
	 *         (instances of ITypingMessage). If <code>null</code>, sending
	 *         typing messages not supported by this provider.
	 */
	public ITypingMessageSender getTypingMessageSender();
	
	/**
	 * Get the history manager for this chat manager.
	 * @return IChatRoomHistoryManager the history manager instance.  Will not be <code>null</code>.
	 */
	public IHistoryManager getHistoryManager();

}
