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
 * Entry poing presence container adapter. For setting up listeners for presence
 * messages, text messages, subscription requests, and for getting interfaces
 * for message sending (IMessageSender) presence updates (IPresenceSender) and
 * account management (IAccountManager)
 * <p>
 * To use this adapter:
 * 
 * <pre>
 *    IPresenceContainerAdapter presenceContainer = (IPresenceContainerAdapter) container.getAdapter(IPresenceContainerAdapter.class);
 *    if (presenceContainer != null) {
 *       ...use presenceContainer
 *    } else {
 *       ...presence not supported by provider
 *    }
 * </pre>
 * 
 */
public interface IPresenceContainerAdapter extends IAdaptable {

	/**
	 * Setup listener for handling roster subscription requests. The given
	 * listener will asynchronously be called when a subscription request is
	 * received by this connected account.
	 * 
	 * @param listener
	 *            for receiving subscription requests. Must not be null.
	 */
	public void addRosterSubscribeListener(IRosterSubscribeListener listener);

	/**
	 * Remove listener for roster subscription requests.
	 * 
	 * @param listener
	 *            the listener to remove
	 */
	public void removeRosterSubscribeListener(IRosterSubscribeListener listener);

	/**
	 * Setup listener for handling presence updates. The given listener will
	 * asynchronously be called when a subscription request is received by this
	 * connected account.
	 * 
	 * @param listener
	 *            for receiving presence notifications. Must not be null.
	 */
	public void addPresenceListener(IPresenceListener listener);

	/**
	 * Remove listener for presence events.
	 * 
	 * @param listener
	 *            the listener to remove
	 */
	public void removePresenceListener(IPresenceListener listener);

	/**
	 * Setup listener for handling IM messages. The given listener will
	 * asynchronously be called when a subscription request is received by this
	 * connected account.
	 * 
	 * @param listener
	 *            for receiving message notifications. Must not be null.
	 */
	public void addMessageListener(IMessageListener listener);

	/**
	 * REmove listener for message events
	 * 
	 * @param listener
	 *            the listener to remove
	 */
	public void removeMessageListener(IMessageListener listener);

	/**
	 * Retrieve interface for sending presence updates. The returned
	 * IPresenceSender (if not null) can be used to send presence change
	 * messages to remote users that have access to the presence information for
	 * the connected account.
	 * 
	 * @return IPresenceSender. Null if no presence sender available for this
	 *         provider.
	 */
	public IPresenceSender getPresenceSender();

	/**
	 * Get interface for sending messages
	 * 
	 * @return IMessageSender. Null if no message sender available
	 */
	public IMessageSender getMessageSender();

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
