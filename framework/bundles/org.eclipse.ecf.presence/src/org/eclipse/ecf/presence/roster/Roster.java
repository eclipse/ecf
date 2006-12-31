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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.ecf.core.user.IUser;

/**
 * Base implementation of IRoster.
 * 
 */
public class Roster implements IRoster {

	private static final long serialVersionUID = 5600691290032864241L;

	protected List rosteritems;

	protected IUser rosterUser;

	public Roster(IUser user) {
		this.rosterUser = user;
		this.rosteritems = Collections.synchronizedList(new ArrayList());
	}

	public Roster() {
		this(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.presence.roster.IRoster#getItems()
	 */
	public Collection getItems() {
		return rosteritems;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.presence.roster.IRosterItem#getName()
	 */
	public String getName() {
		return getUser().getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.presence.roster.IRoster#getUser()
	 */
	public IUser getUser() {
		return rosterUser;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.presence.roster.IRoster#addItem(org.eclipse.ecf.presence.roster.IRosterItem)
	 */
	public boolean addItem(IRosterItem item) {
		if (item == null)
			return false;
		return rosteritems.add(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.presence.roster.IRoster#removeItem(org.eclipse.ecf.presence.roster.IRosterItem)
	 */
	public boolean removeItem(IRosterItem item) {
		if (item == null)
			return false;
		return rosteritems.remove(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.presence.roster.IRoster#setUser(org.eclipse.ecf.core.user.IUser)
	 */
	public void setUser(IUser user) {
		this.rosterUser = user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("Roster[");
		buf.append("user=").append(getUser());
		buf.append("items=").append(getItems()).append("]");
		return buf.toString();
	}
}
