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

import java.util.Collection;

/**
 * Roster group information. The roster group information is associated with
 * {@link IRosterEntry} instances.
 * 
 */
public interface IRosterGroup extends IRosterItem {
	/**
	 * Add a roster item to this group (either IRosterGroup or IRosterEntry).
	 * 
	 * @param item
	 *            the entry to add
	 * @return old roster entry associated with this group, null if not
	 *         previously associated with this group
	 */
	public boolean add(IRosterItem item);

	/**
	 * Remove given roster entry from group
	 * 
	 * @param item
	 *            the entry to remove
	 * @return true if entry removed, false if entry did not previously exist in
	 *         group
	 */
	public boolean remove(IRosterItem item);

	/**
	 * Get roster entries belonging to this group. Instances of list
	 * are of type {@link IRosterEntry}
	 * 
	 * @return Collection of roster entries. Will not return null.
	 */
	public Collection getEntries();

}
