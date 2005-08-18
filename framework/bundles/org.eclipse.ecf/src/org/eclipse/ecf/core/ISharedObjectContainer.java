/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core;

/**
 * Core interface that must be implemented by all ECF container instances.
 * Instances are typically created via {@link SharedObjectContainerFactory}
 */
public interface ISharedObjectContainer extends IReliableContainer {
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
	 * Get SharedObjectManager for this container
	 * 
	 * @return ISharedObjectManager for this container instance
	 */
	public ISharedObjectManager getSharedObjectManager();
}