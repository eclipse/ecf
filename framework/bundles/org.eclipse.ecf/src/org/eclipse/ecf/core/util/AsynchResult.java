/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.util;

import java.lang.reflect.InvocationTargetException;

/**
 * Class to represent asynchronous result (aka Future)
 * 
 */
public class AsynchResult {
	protected Object resultValue = null;

	protected boolean resultReady = false;

	protected InvocationTargetException resultException = null;

	public AsynchResult() {
	}

	/**
	 * Set the underlying function call that will return a result asynchronously
	 * 
	 * @param function
	 *            the {@link ICallable} to be called
	 * @return Runnable to run
	 */
	public Runnable setter(final ICallable function) {
		return new Runnable() {
			public void run() {
				try {
					set(function.call());
				} catch (Throwable ex) {
					setException(ex);
				}
			}
		};
	}

	protected Object doGet() throws InvocationTargetException {
		if (resultException != null)
			throw resultException;
		else
			return resultValue;
	}

	/**
	 * Get the underlying result. Block until result is available.
	 * 
	 * @return Object that is result
	 * @throws InterruptedException
	 *             thrown if waiting is interrupted
	 * @throws InvocationTargetException
	 *             thrown if exception was thrown by execution
	 */
	public synchronized Object get() throws InterruptedException, InvocationTargetException {
		while (!resultReady)
			wait();
		return doGet();
	}

	/**
	 * Get the underlying result with limited wait time. Behaves similarly to
	 * {@link #wait()}, but only waits msecs (ms) before throwing
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
	public synchronized Object get(long msecs) throws TimeoutException, InterruptedException, InvocationTargetException {
		long startTime = (msecs <= 0) ? 0 : System.currentTimeMillis();
		long waitTime = msecs;
		if (resultReady)
			return doGet();
		else if (waitTime <= 0)
			throw new TimeoutException(msecs);
		else {
			for (;;) {
				wait(waitTime);
				if (resultReady)
					return doGet();
				else {
					waitTime = msecs - (System.currentTimeMillis() - startTime);
					if (waitTime <= 0)
						throw new TimeoutException(msecs);
				}
			}
		}
	}

	/**
	 * Set the result to a newValue.
	 * 
	 * @param newValue
	 *            to set the result to
	 */
	public synchronized void set(Object newValue) {
		resultValue = newValue;
		resultReady = true;
		notifyAll();
	}

	/**
	 * Set exception to ex
	 * 
	 * @param ex
	 *            the Throwable to set the exception to
	 */
	public synchronized void setException(Throwable ex) {
		resultException = new InvocationTargetException(ex);
		resultReady = true;
		notifyAll();
	}

	/**
	 * Get the InvocationTargetException that occured during invocation. If
	 * null, no exception was thrown
	 * 
	 * @return InvocationTargetException if an exception occurred (available via
	 *         {@link InvocationTargetException#getCause()}. Null if no
	 *         exception has occurred
	 */
	public synchronized InvocationTargetException getException() {
		return resultException;
	}

	/**
	 * @return true if result has been set or exception has occurred, false if
	 *         not.
	 */
	public synchronized boolean isReady() {
		return resultReady;
	}

	/**
	 * @return Object result that has been set or null if has not been set
	 */
	public synchronized Object peek() {
		return resultValue;
	}

	/**
	 * Clear this AsynchResult. Clears both the result Object and the
	 * InvocationTargetException (if set)
	 */
	public synchronized void clear() {
		resultValue = null;
		resultException = null;
		resultReady = false;
	}
}