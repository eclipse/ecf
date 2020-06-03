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

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.events.ISharedObjectEvent;
import org.eclipse.ecf.core.sharedobject.util.QueueException;

/**
 * Implementers which represent the one-way associations between SharedObject
 * instances within the scope of a given ISharedObjectContainer
 * 
 * @see ISharedObjectManager#connectSharedObjects(ID, ID[])
 */
public interface ISharedObjectConnector {
	/**
	 * Get sender ID for connector
	 * 
	 * @return ID of shared object that is sender for this connection. Will not
	 *         return null
	 */
	public ID getSenderID();

	/**
	 * Get receiver IDs for connector
	 * 
	 * @return ID[] of the shared objects that are the receivers for this
	 *         connection. Will not return null, but may return empty ID[]
	 */
	public ID[] getReceiverIDs();

	/**
	 * Enqueue an ISharedObjectEvent to all the receivers for connector
	 * 
	 * @param event
	 *            to enqueue. Must not be null.
	 * @throws QueueException
	 *             thrown if some problem enqueing to any receivers
	 */
	public void enqueue(ISharedObjectEvent event) throws QueueException;

	/**
	 * Enqueue a set of ISharedObjectEvents to all the receivers for connector
	 * 
	 * @param events []
	 *            of events to enqueue. Must not be null.
	 * @throws QueueException
	 *             thrown if some problem enqueing to any receivers
	 */
	public void enqueue(ISharedObjectEvent[] events) throws QueueException;

	/**
	 * Dispose of this ISharedObjectConnector
	 */
	public void dispose();
}