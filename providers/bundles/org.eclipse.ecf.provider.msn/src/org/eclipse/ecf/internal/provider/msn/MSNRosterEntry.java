/****************************************************************************
 * Copyright (c) 2006, 2007 Remy Suen, Composent Inc., and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.msn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.user.IUser;
import org.eclipse.ecf.presence.IPresence;
import org.eclipse.ecf.presence.roster.IRoster;
import org.eclipse.ecf.presence.roster.IRosterEntry;
import org.eclipse.ecf.presence.roster.IRosterItem;
import org.hantsuki.gokigenyou.Contact;
import org.hantsuki.gokigenyou.Status;

final class MSNRosterEntry implements IPresence, IRosterEntry, IUser {

	private static final long serialVersionUID = 5358415024505371809L;

	private Collection groups;

	private MSNRosterGroup parent;

	private final Contact contact;

	private MSNID id;

	private IRoster roster;

	MSNRosterEntry(IRoster roster, Contact contact, Namespace namespace) {
		this.roster = roster;
		this.contact = contact;
		groups = Collections.EMPTY_LIST;
		try {
			id = (MSNID) namespace.createInstance(new Object[] {
					contact.getEmail(), contact.getDisplayName() });
		} catch (IDCreateException e) {
			e.printStackTrace();
		}
	}

	Contact getContact() {
		return contact;
	}

	public String getName() {
		return contact.getDisplayName();
	}

	public Mode getMode() {
		Status status = contact.getStatus();
		if (status == Status.ONLINE) {
			return Mode.AVAILABLE;
		} else if (status == Status.BUSY) {
			return Mode.DND;
		} else if (status == Status.APPEAR_OFFLINE) {
			return Mode.INVISIBLE;
		} else {
			return Mode.AWAY;
		}
	}

	public Map getProperties() {
		return Collections.EMPTY_MAP;
	}

	public String getStatus() {
		return contact.getPersonalMessage();
	}

	public Type getType() {
		return contact.getStatus() == Status.OFFLINE ? Type.UNAVAILABLE
				: Type.AVAILABLE;
	}

	public Object getAdapter(Class adapter) {
		if (adapter != null && adapter.isInstance(this)) {
			return this;
		} else {
			return null;
		}
	}

	public Collection getGroups() {
		return groups;
	}

	public IPresence getPresence() {
		return this;
	}

	public IUser getUser() {
		return this;
	}
	
	void updateUserName() {
		id.setUserName(contact.getDisplayName());
	}

	void setParent(MSNRosterGroup parent) {
		this.parent = parent;
		ArrayList list = new ArrayList(1);
		list.add(parent);
		groups = Collections.unmodifiableCollection(list);
	}

	public IRosterItem getParent() {
		return parent;
	}

	public byte[] getPictureData() {
		// TODO: update this when avatars have been implemented
		return null;
	}

	public ID getID() {
		return id;
	}

	public String getNickname() {
		return contact.getDisplayName();
	}

	public IRoster getRoster() {
		return roster;
	}

}
