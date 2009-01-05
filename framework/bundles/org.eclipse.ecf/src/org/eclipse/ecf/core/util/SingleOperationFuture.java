/*******************************************************************************
* Copyright (c) 2008 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.core.util;

import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;

public class SingleOperationFuture implements IFuture {

	private Object resultValue = null;
	private IStatus status = null;
	private final IProgressMonitor progressMonitor;
	private TimeoutException timeoutException = null;

	public SingleOperationFuture(IProgressMonitor progressMonitor) {
		this.progressMonitor = new SingleOperationFutureProgressMonitor(this, (progressMonitor == null) ? new NullProgressMonitor() : progressMonitor);
	}

	public SingleOperationFuture() {
		this((IProgressMonitor) null);
	}

	public synchronized Object get() throws InterruptedException, OperationCanceledException {
		throwIfCanceled();
		while (!isDone())
			wait();
		throwIfCanceled();
		return resultValue;
	}

	public synchronized Object get(long waitTimeInMillis) throws InterruptedException, TimeoutException, OperationCanceledException {
		// If we've been canceled then throw
		throwIfCanceled();
		// If we've previously experienced a timeout then throw
		if (timeoutException != null)
			throw timeoutException;
		// Compute start time and waitTime
		long startTime = (waitTimeInMillis <= 0) ? 0 : System.currentTimeMillis();
		long waitTime = waitTimeInMillis;
		// If waitTime out of bounds then throw timeout exception
		if (waitTime <= 0)
			throw createTimeoutException(waitTimeInMillis);
		// If we're already done, then return result
		if (isDone())
			return resultValue;
		// Otherwise, wait for some time, then throw if canceled during wait, return value if
		// we've received one during wait or throw timeout exception if too much time has elapsed
		for (;;) {
			wait(waitTime);
			throwIfCanceled();
			if (isDone())
				return resultValue;
			waitTime = waitTimeInMillis - (System.currentTimeMillis() - startTime);
			if (waitTime <= 0)
				throw createTimeoutException(waitTimeInMillis);
		}
	}

	public IProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	public synchronized boolean isDone() {
		return (status != null);
	}

	protected synchronized void setCanceled() {
		setStatus(new Status(IStatus.ERROR, "org.eclipse.equinox.future", IStatus.ERROR, "Operation canceled", null)); //$NON-NLS-1$ //$NON-NLS-2$
		notifyAll();
	}

	/**
	 * Set the underlying function call that will return a result asynchronously
	 * 
	 * @param function
	 *            the {@link IProgressRunnable} to be called
	 * @return Runnable to run in separate thread
	 */
	public Runnable setter(final IProgressRunnable function) {
		return new Runnable() {
			public void run() {
				try {
					set(function.run(getProgressMonitor()));
				} catch (Throwable ex) {
					setException(ex);
				}
			}
		};
	}

	public synchronized void setException(Throwable ex) {
		setStatus(new Status(IStatus.ERROR, "org.eclipse.equinox.future", IStatus.ERROR, "Exception during operation", ex)); //$NON-NLS-1$ //$NON-NLS-2$
		notifyAll();
	}

	public synchronized void set(Object newValue) {
		resultValue = newValue;
		setStatus(Status.OK_STATUS);
		notifyAll();
	}

	public synchronized IStatus getStatus() {
		return status;
	}

	private synchronized void setStatus(IStatus status) {
		this.status = status;
	}

	private TimeoutException createTimeoutException(long timeout) {
		setStatus(new Status(IStatus.ERROR, "org.eclipse.equinox.future", IStatus.ERROR, NLS.bind("Operation timeout after {0}ms", new Long(timeout)), null)); //$NON-NLS-1$ //$NON-NLS-2$
		timeoutException = new TimeoutException("Timout", timeout); //$NON-NLS-1$
		return timeoutException;
	}

	private void throwIfCanceled() throws OperationCanceledException {
		IProgressMonitor pm = getProgressMonitor();
		if (pm != null && pm.isCanceled()) {
			throw new OperationCanceledException("Operation canceled"); //$NON-NLS-1$
		}
	}

	public boolean hasValue() {
		// for a single operation future, hasValue means that the single 
		// operation has completed, and there will be no more.
		return isDone();
	}

}
