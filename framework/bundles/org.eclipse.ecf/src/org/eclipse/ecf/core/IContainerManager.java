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

package org.eclipse.ecf.core;

import org.eclipse.ecf.core.identity.ID;

/**
 * Container manager for getting access to existing container instances
 * previously created via {@link IContainerFactory}.
 */
public interface IContainerManager {

	/**
	 * Get container for given ID. If <code>containerID</code> is
	 * <code>, null will be returned.  If 
	 * active container with given <code>containerID,</code> is not known to this container manager,
	 * then <code>null</code> will also be returned.
	 * @param containerID the ID of the container instance to retrieve from this manager.  If <code>null</code>
	 * <code>null</code> will be returned.
	 * @return IContainer instance with given <code>containerID</code>.  Will be <code>null</code> if there
	 * is no container with given ID known to this container manager.
	 */
	public IContainer getContainer(ID containerID);

	/**
	 * Get all containers known to this container manager.
	 * 
	 * @return IContainer[] of active container instances known to this
	 *         container manager. Will not return <code>null</code>, but may
	 *         return empty IContainer[].
	 */
	public IContainer[] getAllContainers();

	/**
	 * Return true if this container manager has the given container under
	 * management, false otherwise.
	 * 
	 * @param containerID
	 *            the ID of the container to find. If <code>null</code> this
	 *            method returns false.
	 * 
	 * @return true if this container manager has the given container under
	 *         management, false otherwise.
	 */
	public boolean hasContainer(ID containerID);

	/**
	 * Add given container to manager.
	 * 
	 * @param container
	 *            to add. Must not be <code>null</code>. Also
	 *            <code>container.getID()</code> must return a non-<code>null</code>
	 *            value. If <code>container.getID()</code> returns
	 *            <code>null</code> then this method will throw a
	 *            {@link NullPointerException}.
	 * @return IContainer previously added (with same ID).
	 */
	public IContainer addContainer(IContainer container);

	/**
	 * Remove given container from manager.
	 * @param container the container to remove.  Must not be <code>null</code>. 
	 * 
	 * @return IContainer instance removed.  If no instance with same ID is found
	 * then <code>null</code> will be returned.
	 */
	public IContainer removeContainer(IContainer container);
}
