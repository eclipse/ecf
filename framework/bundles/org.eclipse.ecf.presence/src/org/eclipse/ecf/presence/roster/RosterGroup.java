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
import java.util.Iterator;
import java.util.List;

/**
 * Base roster group class implementing {@link IRosterGroup}. Subclasses may be
 * created as appropriate
 * 
 */
public class RosterGroup extends RosterItem implements IRosterGroup {

	protected List entries;

	public RosterGroup(Object parent, String name,
			Collection /* <IRosterEntry> */existingEntries) {
		super(parent, name);
		entries = Collections.synchronizedList(new ArrayList());
		if (existingEntries != null)
			addAll(existingEntries);
	}

	public RosterGroup(Object parent, String name) {
		this(parent, name, null);
	}

	public boolean add(IRosterItem item) {
		if (item == null)
			return false;
		if (entries.add(item))
			return true;
		else
			return false;
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
	 * @see org.eclipse.ecf.presence.roster.IRosterGroup#remove(org.eclipse.ecf.presence.roster.IRosterItem)
	 */
	public boolean remove(IRosterItem item) {
		return entries.remove(item);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("RosterGroup[");
		sb.append("name=").append(name).append(";");
		sb.append("entries=").append(entries).append(";");
		sb.append("]");
		return sb.toString();
	}

}
