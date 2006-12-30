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
package org.eclipse.ecf.presence.roster;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ecf.presence.IPresenceListener;
import org.eclipse.ecf.presence.IPresenceSender;

/**
 * Roster manager for getting access to and changing roster.
 * 
 */
public interface IRosterManager extends IAdaptable {

	/**
	 * Get roster for this account. Will not be null.
	 * 
	 * @return IRoster for this roster manager. Will not be null.
	 */
	public IRoster getRoster();

	/**
	 * Add roster update listener to receive roster update events for this
	 * roster manager
	 * 
	 * @param listener
	 *            the listener to add. Will not be null.
	 */
	public void addRosterUpdateListener(IRosterUpdateListener listener);

	/**
	 * Remove roster update listener
	 * 
	 * @param listener
	 *            the listener to remove. Will not be null.
	 */
	public void removeRosterUpdateListener(IRosterUpdateListener listener);

	/**
	 * Setup listener for handling roster subscription requests. The given
	 * listener will asynchronously be called when a subscription request is
	 * received by this connected account.
	 * 
	 * @param listener
	 *            for receiving subscription requests. Will not be null.
	 */
	public void addRosterSubscriptionListener(
			IRosterSubscriptionListener listener);

	/**
	 * Remove listener for roster subscription requests.
	 * 
	 * @param listener
	 *            the listener to remove. Will not be null.
	 */
	public void removeRosterSubscriptionListener(
			IRosterSubscriptionListener listener);

	/**
	 * Get roster subscription sender. The roster subscription sender returned
	 * by this method, if not null, may be used to send roster subscribe and
	 * unsubscribe requests
	 * 
	 * @return IRosterSubscriptionSender the sender to use. If null, sending
	 *         requests for roster updates are not supported.
	 */
	public IRosterSubscriptionSender getRosterSubscriptionSender();

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
	 * Setup listener for handling presence updates. The given listener will
	 * asynchronously be called when a subscription request is received by this
	 * connected account.
	 * 
	 * @param listener
	 *            for receiving presence notifications. Must not be null.
	 * 
	 */
	public void addPresenceListener(IPresenceListener listener);

	/**
	 * Remove listener for presence events.
	 * 
	 * @param listener
	 *            the listener to remove.  Must not be null.
	 * 
	 */
	public void removePresenceListener(IPresenceListener listener);


}
