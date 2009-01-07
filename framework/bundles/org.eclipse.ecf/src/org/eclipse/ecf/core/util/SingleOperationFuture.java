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

public class SingleOperationFuture extends AbstractFuture {

	private Object resultValue = null;
	private IStatus status = null;
	private TimeoutException timeoutException = null;

	public SingleOperationFuture() {
		this((IProgressMonitor) null);
	}

	public SingleOperationFuture(IProgressMonitor progressMonitor) {
		super();
		setProgressMonitor(new FutureProgressMonitor(this, (progressMonitor == null) ? new NullProgressMonitor() : progressMonitor));
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

	public synchronized boolean isDone() {
		return (status != null);
	}

	/**
	 * This method is not intended to be called by clients.  Rather, it should only be used by {@link IExecutor}s.
	 * 
	 * @noreference
	 */
	public synchronized void setCanceled() {
		setStatus(new Status(IStatus.ERROR, "org.eclipse.equinox.future", IStatus.ERROR, "Operation canceled", null)); //$NON-NLS-1$ //$NON-NLS-2$
		notifyAll();
	}

	/**
	 * This method is not intended to be called by clients.  Rather it should only be used by {@link IExecutor}s.
	 * 
	 * @noreference
	 */
	public void safeRun(IProgressRunnable runnable) {
		try {
			set(runnable.run(getProgressMonitor()));
		} catch (Throwable t) {
			setException(t);
		}
	}

	private synchronized void setException(Throwable ex) {
		setStatus(new Status(IStatus.ERROR, "org.eclipse.equinox.future", IStatus.ERROR, "Exception during operation", ex)); //$NON-NLS-1$ //$NON-NLS-2$
		notifyAll();
	}

	private synchronized void set(Object newValue) {
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
		setStatus(new Status(IStatus.ERROR, "org.eclipse.equinox.future", IStatus.ERROR, "Operation timeout after " + timeout + "ms", null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
