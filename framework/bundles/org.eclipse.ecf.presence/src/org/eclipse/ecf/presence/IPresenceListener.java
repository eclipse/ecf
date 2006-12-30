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

import org.eclipse.ecf.presence.roster.IRosterEntry;

/**
 * Listener for receiving and processing presence and roster update events.
 * 
 */
public interface IPresenceListener extends IParticipantListener {

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

}
