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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Base roster group class.
 * 
 * @deprecated  See replacement interface and implementation in <code>org.eclipse.ecf.presence.roster</code> package
 */
public class RosterGroup implements IRosterGroup {

	protected List entries;

	protected String name;

	public RosterGroup(String name,
			Collection /* <IRosterEntry> */existingEntries) {
		super();
		this.name = name;
		entries = new ArrayList();
		if (existingEntries != null)
			addAll(existingEntries);
	}

	public RosterGroup(String name) {
		this(name, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.ui.presence.IRosterGroup#add(org.eclipse.ecf.core.identity.ID,
	 *      org.eclipse.ecf.ui.presence.IRosterEntry)
	 */
	public boolean add(IRosterEntry entry) {
		if (entry == null) return false;
		synchronized (entries) {
			entries.add(entry);
			if (entry.add(this)) return true;
			else return false;
		}
	}

	protected void addAll(Collection /* <IRosterEntry> */existingEntries) {
		if (existingEntries == null)
			return;
		synchronized (entries) {
			for (Iterator i = existingEntries.iterator(); i.hasNext();) {
				add((IRosterEntry) i.next());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.ui.presence.IRosterGroup#getRosterEntries()
	 */
	public Collection getEntries() {
		return entries;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.ui.presence.IRosterGroup#getName()
	 */
	public String getName() {
		return this.name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.ui.presence.IRosterGroup#removeEntry(org.eclipse.ecf.ui.presence.IRosterEntry)
	 */
	public boolean remove(IRosterEntry entry) {
		synchronized (entries) {
			return entries.remove(entry);
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("RosterGroup[");
		sb.append("name=").append(name).append(";");
		sb.append("entries=").append(entries).append(";");
		sb.append("]");
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

}
