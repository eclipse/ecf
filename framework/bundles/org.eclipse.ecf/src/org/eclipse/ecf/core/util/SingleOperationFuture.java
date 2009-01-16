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
import org.eclipse.ecf.internal.core.ECFPlugin;

/**
 * Future implementation for a single operation.
 * 
 */
public class SingleOperationFuture extends AbstractFuture {

	private Object resultValue = null;
	private IStatus status = null;
	private TimeoutException timeoutException = null;
	protected IProgressMonitor progressMonitor;

	public SingleOperationFuture() {
		this((IProgressMonitor) null);
	}

	public SingleOperationFuture(IProgressMonitor progressMonitor) {
		super();
		setProgressMonitor(new FutureProgressMonitor((progressMonitor == null) ? new NullProgressMonitor() : progressMonitor));
	}

	public synchronized Object get() throws InterruptedException, OperationCanceledException {
		throwIfCanceled();
		while (!isDone())
			wait();
		throwIfCanceled();
		return resultValue;
	}

	public synchronized Object get(long waitTimeInMillis) throws InterruptedException, TimeoutException, OperationCanceledException {
		// If waitTime out of bounds then throw illegal argument exception
		if (waitTimeInMillis < 0)
			throw new IllegalArgumentException("waitTimeInMillis must be => 0"); //$NON-NLS-1$
		// If we've been canceled then throw
		throwIfCanceled();
		// If we've previously experienced a timeout then throw
		if (timeoutException != null)
			throw timeoutException;
		// If we're already done, then return result
		if (isDone())
			return resultValue;
		// Otherwise, wait for some time, then throw if canceled during wait, return value if
		// Compute start time and set waitTime
		long startTime = System.currentTimeMillis();
		long waitTime = waitTimeInMillis;
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
	 * This method is not intended to be called by clients.  Rather it should only be used by {@link IExecutor}s.
	 * 
	 * @noreference
	 */
	public void runWithProgress(final IProgressRunnable runnable) {
		Assert.isNotNull(runnable);
		if (!isCanceled()) {
			SafeRunner.run(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					if (!isCanceled())
						setException(exception);
				}

				public void run() throws Exception {
					Object result = runnable.run(getProgressMonitor());
					if (!isCanceled())
						set(result);
				}
			});
		}
	}

	public synchronized IStatus getStatus() {
		return status;
	}

	public boolean hasValue() {
		// for a single operation future, hasValue means that the single 
		// operation has completed, and there will be no more.
		return isDone();
	}

	public synchronized boolean cancel() {
		if (isDone())
			return false;
		if (isCanceled())
			return false;
		setStatus(new Status(IStatus.CANCEL, ECFPlugin.PLUGIN_ID, IStatus.CANCEL, "Operation canceled", null)); //$NON-NLS-1$
		getProgressMonitor().setCanceled(true);
		notifyAll();
		return true;
	}

	protected synchronized void setException(Throwable ex) {
		setStatus(new Status(IStatus.ERROR, ECFPlugin.PLUGIN_ID, IStatus.ERROR, "Exception during operation", ex)); //$NON-NLS-1$
		notifyAll();
	}

	protected synchronized void set(Object newValue) {
		resultValue = newValue;
		setStatus(Status.OK_STATUS);
		notifyAll();
	}

	private synchronized void setStatus(IStatus status) {
		this.status = status;
	}

	private TimeoutException createTimeoutException(long timeout) {
		setStatus(new Status(IStatus.ERROR, ECFPlugin.PLUGIN_ID, IStatus.ERROR, "Operation timeout after " + timeout + "ms", null)); //$NON-NLS-1$ //$NON-NLS-2$
		timeoutException = new TimeoutException("Single operation timeout", timeout); //$NON-NLS-1$
		return timeoutException;
	}

	private void throwIfCanceled() throws OperationCanceledException {
		IProgressMonitor pm = getProgressMonitor();
		if (pm != null && pm.isCanceled()) {
			throw new OperationCanceledException("Single operation canceled"); //$NON-NLS-1$
		}
	}

	protected synchronized void setProgressMonitor(IProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

	public synchronized IProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	public synchronized boolean isCanceled() {
		return getProgressMonitor().isCanceled();
	}

	public Object getAdapter(Class adapter) {
		if (adapter == null)
			return null;
		if (adapter.isInstance(this)) {
			return this;
		}
		final IAdapterManager adapterManager = ECFPlugin.getDefault().getAdapterManager();
		if (adapterManager == null)
			return null;
		return adapterManager.loadAdapter(this, adapter.getName());
	}

}
