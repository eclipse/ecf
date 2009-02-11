/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.core.util;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.equinox.concurrent.future.TimeoutException;

/**
 *
 */
public interface IAsyncResult {

	/**
	 * Get the underlying result. Block until result is available.
	 * 
	 * @return Object that is result
	 * @throws InterruptedException
	 *             thrown if waiting is interrupted
	 * @throws InvocationTargetException
	 *             thrown if exception was thrown by execution
	 */
	public abstract Object get() throws InterruptedException, InvocationTargetException;

	/**
	 * Get the underlying result with limited wait time. Behaves similarly to
	 * {@link Object#wait()}, but only waits msecs (ms) before throwing
	 * TimeoutException
	 * 
	 * @param msecs
	 *            to wait before timing out
	 * @return Object that is result
	 * @throws TimeoutException
	 *             thrown if msecs elapse before a result is available
	 * @throws InterruptedException
	 *             thrown if waiting is interrupted
	 * @throws InvocationTargetException
	 *             thrown if exception was thrown by execution
	 */
	public abstract Object get(long msecs) throws TimeoutException, InterruptedException, InvocationTargetException;

	/**
	 * Get the InvocationTargetException that occured during invocation. If
	 * null, no exception was thrown
	 * 
	 * @return InvocationTargetException if an exception occurred. <code>Null</code> if no
	 *         exception has occurred
	 */
	public abstract InvocationTargetException getException();

	/**
	 * @return true if result has been set or exception has occurred, false if
	 *         not.
	 */
	public abstract boolean isReady();

	/**
	 * @return Object result that has been set or null if has not been set
	 */
	public abstract Object peek();

	/**
	 * Clear this AsyncResult. Clears both the result Object and the
	 * InvocationTargetException (if set)
	 */
	public abstract void clear();

}