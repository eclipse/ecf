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
package org.eclipse.ecf.core.sharedobject;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.Event;

/**
 * Core interface for implementing components that exist within
 * {@link ISharedObjectContainer}
 * 
 */
public interface ISharedObject extends IAdaptable {
	/**
	 * Initialize this ISharedObject. The ISharedObjectContainer for this
	 * ISharedObject must call this method with a non-null instance of
	 * ISharedObjectConfig. ISharedObject implementations can use this
	 * initialization to perform any initialization necessary prior to receiving
	 * any events (via handleEvent/s). Note that the ISharedObjectContext
	 * provided via the ISharedObjectConfig.getSharedObjectContext() method is
	 * not guaranteed to allow any method calls until after this init() method
	 * call has completed.
	 * 
	 * @param initData
	 *            the initialization data passed by the ISharedObjectContainer
	 *            upon initialization
	 * @exception SharedObjectInitException
	 *                thrown by ISharedObject to halt initialization.
	 *                ISharedObjectContainers must respond to such an exception
	 *                by halting the addition of the ISharedObject instance and
	 *                treating it as <b>not </b> in the container.
	 */
	public void init(ISharedObjectConfig initData)
			throws SharedObjectInitException;

	/**
	 * Handle Event passed to this IEventHandler.
	 * 
	 * @param event
	 *            the Event for the implementer to handle
	 */
	public void handleEvent(Event event);

	/**
	 * Handle Events passed to this IEventHandler.
	 * 
	 * @param events
	 *            the Events [] for the implementer to handle
	 */
	public void handleEvents(Event[] events);

	/**
	 * Method called by the ISharedObjectContainer upon ISharedObject
	 * destruction. Once this method is called, no more Events will be passed to
	 * a ISharedObject until the init method is called again.
	 * 
	 * @param containerID
	 *            the ID of the container that is disposing this ISharedObject
	 */
	public void dispose(ID containerID);
}