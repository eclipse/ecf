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
package org.eclipse.ecf.core.sharedobject.util;

/**
 * Simple queue
 * 
 */
public interface ISimpleFIFOQueue {
	/**
	 * Enqueue given object. Blocks until enqueue is completed.
	 * 
	 * @param obj
	 *            the Object to enqueue
	 * @return true if enqueued, false if not successfully enqueue
	 */
	public boolean enqueue(Object obj);

	/**
	 * Dequeue an object from off the
	 * 
	 * @return Object dequeued
	 */
	public Object dequeue();

	/**
	 * @return Object at head of queue without removing it from queue
	 */
	public Object peekQueue();

	/**
	 * @return Object that is head of queue. Removes head from queue
	 */
	public Object removeHead();

	/**
	 * Close this queue. Once closed, the underlying queue cannot be used again
	 */
	public void close();
}