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

import java.util.Iterator;

/**
 * Roster group information. The roster group information is associated with
 * {@link IRosterEntry} instances.
 * 
 */
public interface IRosterGroup {
	/**
	 * Add a roster entry to this group
	 * 
	 * @param entry
	 *            the entry to add
	 * @return old roster entry associated with this group, null if not
	 *         previously associated with this group
	 */
	public IRosterEntry add(IRosterEntry entry);

	/**
	 * This roster group contains the given roster entry
	 * 
	 * @param entry
	 *            the entry to check
	 * @return true if the group does contain the entry, false otherwise
	 */
	public boolean contains(IRosterEntry entry);

	/**
	 * Get Iterator of roster entries belonging to this group. Instances of list
	 * are of type {@link IRosterEntry}
	 * 
	 * @return Iterator of roster entries. Will not return null.
	 */
	public Iterator getRosterEntries();

	/**
	 * Return number of roster entries currently in group.
	 * 
	 * @return number of entries
	 */
	public int size();

	/**
	 * Return name of group.
	 * 
	 * @return String name of group. May return null.
	 */
	public String getName();

	/**
	 * Remove given roster entry from group
	 * 
	 * @param entry
	 *            the entry to remove
	 * @return true if entry removed, false if entry did not previously exist in
	 *         group
	 */
	public boolean removeEntry(IRosterEntry entry);
}
