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
import org.eclipse.ecf.presence.chatroom.IChatRoomManager;
import org.eclipse.ecf.presence.im.IChatManager;
import org.eclipse.ecf.presence.roster.IRosterManager;

/**
 * Entry point presence container adapter. For setting up listeners for presence
 * messages, text messages, subscription requests, and for getting interfaces
 * for message sending (IMessageSender) presence updates (IPresenceSender) and
 * account management (IAccountManager)
 * <p>
 * To use this adapter:
 * 
 * <pre>
 *           IPresenceContainerAdapter presenceContainer = (IPresenceContainerAdapter) container.getAdapter(IPresenceContainerAdapter.class);
 *           if (presenceContainer != null) {
 *              ...use presenceContainer
 *           } else {
 *              ...presence not supported by provider
 *           }
 * </pre>
 * 
 */
public interface IPresenceContainerAdapter extends IAdaptable {

	/**
	 * Get roster manager for access to roster model. If null is returned roster
	 * manager unavailable for this adapter.
	 * 
	 * @return IRosterManager if available for this adapter. Null if not
	 *         available for for the implementing provider.
	 */
	public IRosterManager getRosterManager();

	/**
	 * Get chat manager for sending and receiving chat messages
	 * 
	 * @return IChatManager for this presence container adapter. Null if no chat
	 *         manager available for given provider.
	 */
	public IChatManager getChatManager();

	/**
	 * Get interface for managing account
	 * 
	 * @return IAccountManger. Null if no account manager available
	 */
	public IAccountManager getAccountManager();

	/**
	 * Get chat room manager for this presence container. If returns null, no
	 * chat room facilities are available
	 * 
	 * @return a chat room manager instance if chat room facilities are
	 *         available for this presence container If no such facilities are
	 *         available, returns null
	 */
	public IChatRoomManager getChatRoomManager();
}
