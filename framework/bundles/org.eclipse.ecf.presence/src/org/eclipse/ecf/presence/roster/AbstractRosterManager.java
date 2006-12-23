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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.presence.IPresence;
import org.eclipse.ecf.presence.IPresenceSender;

public abstract class AbstractRosterManager implements IRosterManager {

	protected IRoster roster;

	protected List rosterSubscriptionListeners = new ArrayList();
	protected List rosterUpdateListeners = new ArrayList();

	public AbstractRosterManager() {

	}

	public AbstractRosterManager(IRoster roster) {
		this.roster = roster;
	}

	public synchronized void addRosterSubscriptionListener(
			IRosterSubscriptionListener listener) {
		if (listener == null)
			return;
		rosterSubscriptionListeners.add(listener);
	}

	public synchronized void addRosterUpdateListener(
			IRosterUpdateListener listener) {
		if (listener == null)
			return;
		rosterUpdateListeners.add(listener);
	}

	protected void fireRosterUpdate(IRosterItem changedItem) {
		synchronized (this) {
			for (Iterator i = rosterUpdateListeners.iterator(); i.hasNext();) {
				IRosterUpdateListener l = (IRosterUpdateListener) i.next();
				l.handleRosterUpdate(roster, changedItem);
			}
		}
	}

	protected void fireSubscriptionListener(ID fromID, IPresence presence) {
		synchronized (this) {
			for (Iterator i = rosterSubscriptionListeners.iterator(); i
					.hasNext();) {
				IRosterSubscriptionListener l = (IRosterSubscriptionListener) i
						.next();
				if (presence.getType().equals(IPresence.Type.SUBSCRIBE)) {
					l.handleSubscribeRequest(fromID);
				} else if (presence.getType().equals(IPresence.Type.SUBSCRIBED)) {
					l.handleSubscribed(fromID);
				} else if (presence.getType().equals(
						IPresence.Type.UNSUBSCRIBED)) {
					l.handleUnsubscribed(fromID);
				}
			}
		}
	}

	public abstract IPresenceSender getPresenceSender();

	public IRoster getRoster() {
		return roster;
	}

	public abstract IRosterSubscriptionSender getRosterSubscriptionSender();

	public synchronized void removeRosterSubscriptionListener(
			IRosterSubscriptionListener listener) {
		if (listener == null)
			return;
		rosterSubscriptionListeners.remove(listener);
	}

	public synchronized void removeRosterUpdateListener(
			IRosterUpdateListener listener) {
		if (listener == null)
			return;
		rosterSubscriptionListeners.remove(listener);
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

}
