/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.core;

import org.eclipse.ecf.core.identity.ID;

/**
 * Core interface that must be implemented by all ECF container instances.
 * Instances are typically created via {@link SharedObjectContainerFactory}
 */
public interface ISharedObjectContainer extends IContainer {

	/**
	 * Return the ISharedObjectContainerConfig for this ISharedObjectContainer.
	 * The returned value must always be non-null.
	 * 
	 * @return ISharedObjectContainerConfig for the given ISharedObjectContainer
	 *         instance
	 */
	public ISharedObjectContainerConfig getConfig();

	/**
	 * Add listener to ISharedObjectContainer. Listener will be notified when
	 * container events occur
	 * 
	 * @param l
	 *            the ISharedObjectContainerListener to add
	 * @param filter
	 *            the filter to define types of container events to receive
	 */
	public void addListener(ISharedObjectContainerListener l, String filter);

	/**
	 * Remove listener from ISharedObjectContainer.
	 * 
	 * @param l
	 *            the ISharedObjectContainerListener to remove
	 */
	public void removeListener(ISharedObjectContainerListener l);

	/**
	 * Get the group id that this container has joined. Return null if no group
	 * has previously been joined.
	 * 
	 * @return ID of the group previously joined
	 */
	public ID getGroupID();

	/**
	 * Get the current membership of the joined group. This method will
	 * accurately report the current group membership of the connected group.
	 * 
	 * @return ID[] the IDs of the current group membership
	 */
	public ID[] getGroupMemberIDs();

	/**
	 * @return true if this ISharedObjectContainer instance is in the 'manager'
	 *         role for the group, false otherwise
	 */
	public boolean isGroupManager();

	/**
	 * Get SharedObjectManager for this container
	 * 
	 * @return ISharedObjectManager for this container instance
	 */
	public ISharedObjectManager getSharedObjectManager();

}