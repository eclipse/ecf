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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.internal.core.ECFPlugin;
import org.eclipse.osgi.util.NLS;

/**
 * Class to represent asynchronous result (aka Future)
 * 
 */
public class AsyncResult implements IAsyncResult {
	protected Object resultValue = null;

	protected boolean resultReady = false;

	protected InvocationTargetException resultException = null;

	public AsyncResult() {
		// null constructor
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
		return resultValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.util.IAsyncResult#get()
	 */
	public synchronized Object get() throws InterruptedException, InvocationTargetException {
		while (!resultReady)
			wait();
		return doGet();
	}

	private TimeoutException createTimeoutException(long timeout) {
		return new TimeoutException(new Status(IStatus.ERROR, ECFPlugin.PLUGIN_ID, IStatus.ERROR, NLS.bind("Operation timeout after {0}ms", new Long(timeout)), null), timeout); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.util.IAsyncResult#get(long)
	 */
	public synchronized Object get(long msecs) throws TimeoutException, InterruptedException, InvocationTargetException {
		long startTime = (msecs <= 0) ? 0 : System.currentTimeMillis();
		long waitTime = msecs;
		if (resultReady)
			return doGet();
		else if (waitTime <= 0)
			throw createTimeoutException(msecs);
		else {
			for (;;) {
				wait(waitTime);
				if (resultReady)
					return doGet();
				waitTime = msecs - (System.currentTimeMillis() - startTime);
				if (waitTime <= 0)
					throw createTimeoutException(msecs);
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

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.util.IAsyncResult#getException()
	 */
	public synchronized InvocationTargetException getException() {
		return resultException;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.util.IAsyncResult#isReady()
	 */
	public synchronized boolean isReady() {
		return resultReady;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.util.IAsyncResult#peek()
	 */
	public synchronized Object peek() {
		return resultValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.util.IAsyncResult#clear()
	 */
	public synchronized void clear() {
		resultValue = null;
		resultException = null;
		resultReady = false;
	}
}