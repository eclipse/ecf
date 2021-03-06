/****************************************************************************
 * Copyright (c) 2008 Versant Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Remy Chi Jian Suen (Versant Corporation) - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.example.collab.share;

import org.eclipse.ecf.internal.example.collab.ui.LineChatClientView;
import org.eclipse.ecf.presence.roster.*;

/**
 * 
 * @since 2.0
 */
public class RosterListener implements IRosterListener {

	private final EclipseCollabSharedObject sharedObject;
	private final LineChatClientView view;

	RosterListener(EclipseCollabSharedObject sharedObject, LineChatClientView view) {
		this.sharedObject = sharedObject;
		this.view = view;
	}

	public void handleRosterEntryAdd(IRosterEntry entry) {
		boolean addUserResult = view.addUser(entry.getUser());
		// If addUserResult is false, it means that this is a new user
		// And we need to report our own existence to them
		if (addUserResult)
			sharedObject.sendNotifyUserAdded();
	}

	public void handleRosterEntryRemove(IRosterEntry entry) {
		view.removeUser(entry.getUser().getID());
	}

	public void handleRosterUpdate(IRoster roster, IRosterItem changedValue) {
		// unimplemented, update code has been removed at the moment
	}

}
