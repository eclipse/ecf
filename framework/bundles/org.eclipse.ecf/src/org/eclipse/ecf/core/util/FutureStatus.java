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
import org.eclipse.osgi.util.NLS;

public class FutureStatus implements IFuture {

	class FutureStatusProgressMonitor implements IProgressMonitor {

		private final IProgressMonitor monitor;

		public FutureStatusProgressMonitor(IProgressMonitor progressMonitor) {
			this.monitor = progressMonitor;
		}

		public void beginTask(String name, int totalWork) {
			monitor.beginTask(name, totalWork);
		}

		public void done() {
			monitor.done();
		}

		public void internalWorked(double work) {
			monitor.internalWorked(work);
		}

		public boolean isCanceled() {
			return monitor.isCanceled();
		}

		public void setCanceled(boolean value) {
			monitor.setCanceled(value);
			// If this is intended to cancel
			// the operation, then we also call
			// FutureStatus.this.setCanceled()
			if (value)
				FutureStatus.this.setCanceled();
		}

		public void setTaskName(String name) {
			monitor.setTaskName(name);
		}

		public void subTask(String name) {
			monitor.subTask(name);
		}

		public void worked(int work) {
			monitor.worked(work);
		}

	}

	private Object resultValue = null;
	private IStatus status = null;
	private final IProgressMonitor progressMonitor;

	public FutureStatus(IProgressMonitor progressMonitor) {
		this.progressMonitor = new FutureStatusProgressMonitor((progressMonitor == null) ? new NullProgressMonitor() : progressMonitor);
	}

	public FutureStatus() {
		this(null);
	}

	public synchronized Object get() throws InterruptedException, CanceledException {
		throwIfCanceled();
		while (!isDone())
			wait();
		throwIfCanceled();
		return resultValue;
	}

	public synchronized Object get(long waitTimeInMillis) throws InterruptedException, TimeoutException, CanceledException {
		long startTime = (waitTimeInMillis <= 0) ? 0 : System.currentTimeMillis();
		long waitTime = waitTimeInMillis;
		throwIfCanceled();
		if (isDone())
			return resultValue;
		else if (waitTime <= 0)
			throw createTimeoutException(waitTimeInMillis);
		else {
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
	}

	public IProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	public synchronized boolean isDone() {
		return (status != null);
	}

	synchronized void setCanceled() {
		setStatus(new Status(IStatus.ERROR, ECFPlugin.PLUGIN_ID, IStatus.ERROR, "Operation cancelled", null)); //$NON-NLS-1$
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
		setStatus(new Status(IStatus.ERROR, ECFPlugin.PLUGIN_ID, IStatus.ERROR, "Exception during operation", ex)); //$NON-NLS-1$
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
		setStatus(new Status(IStatus.ERROR, ECFPlugin.PLUGIN_ID, IStatus.ERROR, NLS.bind("Operation timeout after {0}ms", new Long(timeout)), null)); //$NON-NLS-1$
		return new TimeoutException(getStatus(), timeout);
	}

	private void throwIfCanceled() throws CanceledException {
		IProgressMonitor pm = getProgressMonitor();
		if (pm != null && pm.isCanceled()) {
			throw new CanceledException(getStatus());
		}
	}

	public boolean hasValue() {
		return isDone();
	}

}
