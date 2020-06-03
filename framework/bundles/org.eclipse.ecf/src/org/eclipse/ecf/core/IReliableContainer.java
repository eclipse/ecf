/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.core;

import org.eclipse.ecf.core.identity.ID;

/**
 * Contract for reliable container. Extends IContainer
 * 
 * @see IContainer
 */
public interface IReliableContainer extends IContainer {
	/**
	 * Get the current membership of the joined group. This method will
	 * accurately report the current group membership of the connected group.
	 * 
	 * @return ID[] the IDs of the current group membership
	 */
	public ID[] getGroupMemberIDs();

	/**
	 * @return true if this IReliableContainer instance is in the 'manager' role
	 *         for the group, false otherwise.
	 */
	public boolean isGroupManager();
}
