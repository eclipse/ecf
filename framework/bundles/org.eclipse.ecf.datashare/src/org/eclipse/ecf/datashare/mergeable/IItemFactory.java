/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
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
	 *            the actual data associated with an item
	 * @return IItem result
	 */
	public IItem createItem(String description);
}
