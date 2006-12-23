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
 * Listener for receiving and processing presence and roster update events.
 * 
 * @deprecated No longer needed with roster model presented in
 *             <code>org.eclipse.ecf.presence.roster</code>
 */
public interface IPresenceListener extends IParticipantListener {

	/**
	 * Notification that this presence listener has connected to a notification
	 * source (e.g. a presence server).
	 * 
	 * @param connectedID
	 *            the ID of the presence notification source we are now
	 *            connected to.
	 */
	public void handleConnected(ID connectedID);

	/**
	 * Notification that a roster entry has been added
	 * 
	 * @param entry
	 *            the roster entry that has changed
	 */
	public void handleRosterEntryAdd(IRosterEntry entry);

	/**
	 * Notification that a roster entry has been changed
	 * 
	 * @param entry
	 *            the entry that has changed
	 */
	public void handleRosterEntryUpdate(IRosterEntry entry);

	/**
	 * Notification that a roster entry has been renived
	 * 
	 * @param entry
	 *            the entry that has been removed
	 */
	public void handleRosterEntryRemove(IRosterEntry entry);

	/**
	 * Notification that this presence listener has disconnected to a
	 * notification source (e.g. a presence server).
	 * 
	 * @param disconnectedID
	 *            the ID of the presence notification source we are now
	 *            connected to.
	 */
	public void handleDisconnected(ID disconnectedID);

}
