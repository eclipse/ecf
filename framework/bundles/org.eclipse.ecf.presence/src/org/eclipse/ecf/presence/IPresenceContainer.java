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
package org.eclipse.ecf.presence;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ecf.presence.chat.IChatRoomManager;


/**
 * Top-level container interface for setting up listeners for presence messages,
 * text messages, subscription requests, and for getting interfaces for message sending (IMessageSender)
 * presence updates (IPresenceSender) and account management (IAccountManager)
 * 
 */
public interface IPresenceContainer extends IAdaptable {
    
	/**
	 * Setup listener for handling subscription requests
	 * 
	 * @param listener
	 */
	public void addSubscribeListener(ISubscribeListener listener);
    /**
     * Setup listener for handling presence updates
     * @param listener
     */
	public void addPresenceListener(IPresenceListener listener);
    /**
     * Setup listener for handling IM messages
     * @param listener
     */
	public void addMessageListener(IMessageListener listener);

	/**
	 * Get interface for sending presence updates
	 * @return IPresenceSender.  Null if no presence sender available
	 */
    public IPresenceSender getPresenceSender();
	/**
	 * Get interface for sending messages
	 * @return IMessageSender.  Null if no message sender available
	 */
    public IMessageSender getMessageSender();
	/**
	 * Get interface for managing account
	 * @return IAccountManger.  Null if no account manager available
	 */
	public IAccountManager getAccountManager();
	
	/**
	 * Get chat room manager for this presence container.  If returns null, no chat room facilities are available
	 * @return a chat room manager instance if chat room facilities are available for this presence container
	 * If no such facilities are available, returns null
	 */
	public IChatRoomManager getChatRoomManager();
}
