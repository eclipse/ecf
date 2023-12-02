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

package org.eclipse.ecf.core;

/**
 * Container manager listener. Instances of this interface may be registered via
 * calls to {@link IContainerManager#addListener(IContainerManagerListener)}.
 * When subsequent additions to the {@link IContainerManager} occur, the
 * {@link #containerAdded(IContainer)} method will be called. When container
 * removals occur, {@link #containerRemoved(IContainer)}. Note that these
 * methods will be called by arbitrary threads.
 */
public interface IContainerManagerListener {

	/**
	 * Container added to the implementing IContainerManager.
	 * 
	 * @param container
	 *            the {@link IContainer} added. Will not be <code>null</code>.
	 */
	public void containerAdded(IContainer container);

	/**
	 * Container removed from the implementing IContainerManager.
	 * 
	 * @param container
	 *            the {@link IContainer} removed. Will not be <code>null</code>.
	 */
	public void containerRemoved(IContainer container);
}
