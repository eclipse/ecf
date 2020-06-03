/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.presence.roster;

import org.eclipse.core.runtime.IAdaptable;

public interface IRosterItem extends IAdaptable {
	/**
	 * Return name of item.
	 * 
	 * @return String name of item. May return <code>null</code>.
	 */
	public String getName();

	/**
	 * Return parent of item
	 * 
	 * @return IRosterItem parent of roster item. May be <code>null</code>.
	 */
	public IRosterItem getParent();
	
	/**
	 * Get the roster associated with this item.
	 * 
	 * @return IRoster instance associated with this item.  Will return <code>null</code> if
	 * this IRosterItem is not associated with any roster.
	 */
	public IRoster getRoster();
}
