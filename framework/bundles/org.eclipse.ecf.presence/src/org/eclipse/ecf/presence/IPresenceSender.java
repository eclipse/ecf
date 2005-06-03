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
 * @author slewis
 *
 */
public interface IPresenceSender {

	/**
	 * Send a presence update to a remote target user
	 * @param fromID the user the update is from
	 * @param toID the target user
	 * @param presence the presence information
	 */
	public void sendPresenceUpdate(ID fromID, ID toID, IPresence presence);
	/**
	 * Send a roster add request (subscribe) to a remote
	 * @param fromID the user the subscription request is from
	 * @param user the account name of the target user
	 * @param groups an array of group names that this use will belong to
	 * on the roster entry
	 */
	public void sendRosterAdd(ID fromID, String user, String name, String [] groups);
	/**
	 * Send roster remove request (unsubscribe) to a remote
	 * @param fromID the user id the request is from
	 * @param userID the user id the request it intended for
	 */
	public void sendRosterRemove(ID fromID, ID userID);
}
