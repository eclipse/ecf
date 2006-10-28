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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.ecf.core.identity.ID;

/**
 * Base roster group class implementing {@link IRosterGroup}. Subclasses may be
 * created as appropriate
 * 
 */
public class RosterGroup implements IRosterGroup {

	protected Map entries;

	protected String name;

	public RosterGroup(String name, Map existing) {
		super();
		this.name = name;
		entries = new HashMap();
		if (existing != null)
			addAll(existing);
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
	public IRosterEntry add(IRosterEntry entry) {
		if (entry != null) {
			synchronized (entries) {
				IRosterEntry res = (IRosterEntry) entries.put(
						entry.getUserID(), entry);
				entry.add(this);
				return res;
			}
		} else
			return null;
	}

	public void addAll(Map existing) {
		synchronized (entries) {
			for (Iterator i = existing.entrySet().iterator(); i.hasNext();) {
				IRosterEntry entry = (IRosterEntry) i.next();
				add(entry);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.ui.presence.IRosterGroup#contains(org.eclipse.ecf.core.identity.ID)
	 */
	public boolean contains(IRosterEntry entry) {
		if (entry == null)
			return false;
		synchronized (entries) {
			ID uID = entry.getUserID();
			boolean has = entries.containsKey(uID);
			if (has) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.ui.presence.IRosterGroup#getRosterEntries()
	 */
	public Iterator getRosterEntries() {
		return entries.values().iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.ui.presence.IRosterGroup#size()
	 */
	public int size() {
		return entries.size();
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
	public boolean removeEntry(IRosterEntry entry) {
		synchronized (entries) {
			IRosterEntry res = (IRosterEntry) entries.remove(entry.getUserID());
			if (res == null)
				return true;
			else
				return false;
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("RosterGroup[");
		sb.append("name=").append(name).append(";");
		sb.append("entries=").append(entries).append(";");
		sb.append("]");
		return sb.toString();
	}

}
