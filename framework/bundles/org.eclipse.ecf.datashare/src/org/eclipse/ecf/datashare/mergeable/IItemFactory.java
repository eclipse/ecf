/****************************************************************************
 * Copyright (c) 2004 Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.datashare.mergeable;

/**
 * Factory for creating items
 * 
 */
public interface IItemFactory {
	/**
	 * Create item with given description
	 * 
	 * @param description
	 *            the description to add to the item. The description represents
	 *            the actual data associated with an item. Should not be
	 *            <code>null</code>.
	 * @return IItem result. Will not be <code>null</code>.
	 */
	public IItem createItem(String description);
}
