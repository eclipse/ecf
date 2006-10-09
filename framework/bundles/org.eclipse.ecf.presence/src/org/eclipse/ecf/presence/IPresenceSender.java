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

import org.eclipse.ecf.core.identity.ID;

/**
 * Send presence change events to remotes on buddy list
 * 
 */
public interface IPresenceSender {

	/**
	 * Send a presence update to a remote target user
	 * 
	 * @param fromID
	 *            the user the update is from. Should not be null.
	 * @param toID
	 *            the target user. Should not be null.
	 * @param presence
	 *            the presence information. Should not be null.
	 */
	public void sendPresenceUpdate(ID fromID, ID toID, IPresence presence);

	/**
	 * Send a roster add request (subscribe) to a remote
	 * 
	 * @param fromID
	 *            the user the subscription request is from. Should not be null.
	 * @param user
	 *            the account name of the target user. Should not be null.
	 * @param groups
	 *            an array of group names that this use will belong to on the
	 *            roster entry. Should not be null.
	 */
	public void sendRosterAdd(ID fromID, String user, String name,
			String[] groups);

	/**
	 * Send roster remove request (unsubscribe) to a remote
	 * 
	 * @param fromID
	 *            the user id the request is from. Should not be null.
	 * @param userID
	 *            the user id the request it intended for. Should not be null.
	 */
	public void sendRosterRemove(ID fromID, ID userID);
}
