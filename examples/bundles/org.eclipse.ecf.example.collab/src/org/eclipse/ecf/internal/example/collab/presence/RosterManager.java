/******************************************************************************
 * Copyright (c) 2008 Versant Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Remy Chi Jian Suen (Versant Corporation) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.example.collab.presence;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.ecf.core.user.IUser;
import org.eclipse.ecf.presence.IPresenceContainerAdapter;
import org.eclipse.ecf.presence.IPresenceListener;
import org.eclipse.ecf.presence.IPresenceSender;
import org.eclipse.ecf.presence.roster.AbstractRosterManager;
import org.eclipse.ecf.presence.roster.IRosterEntry;
import org.eclipse.ecf.presence.roster.IRosterItem;
import org.eclipse.ecf.presence.roster.IRosterSubscriptionSender;
import org.eclipse.ecf.presence.roster.Roster;

public class RosterManager extends AbstractRosterManager {

	private List presenceListeners = new LinkedList();

	public RosterManager(IPresenceContainerAdapter containerAdapter, IUser user) {
		super();

		roster = new Roster(containerAdapter, user) {
			public boolean addItem(IRosterItem item) {
				if (super.addItem(item)) {
					fireRosterAdd((IRosterEntry) item);
					return true;
				}
				return false;
			}
			
			public boolean removeItem(IRosterItem item) {
				if (super.removeItem(item)) {
					fireRosterRemove((IRosterEntry) item);
					return true;
				}
				return false;
			}
		};
	}

	public IPresenceSender getPresenceSender() {
		return null;
	}

	public IRosterSubscriptionSender getRosterSubscriptionSender() {
		return null;
	}

	public void addPresenceListener(IPresenceListener listener) {
		if (!presenceListeners.contains(listener)) {
			presenceListeners.add(listener);
		}
	}

	public void removePresenceListener(IPresenceListener listener) {
		presenceListeners.remove(listener);
	}

}
