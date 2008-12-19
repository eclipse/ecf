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

public class FutureStatus extends Status implements IFutureStatus {

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
	private boolean resultReady = false;
	private int futureSeverity;
	private IProgressMonitor progressMonitor = null;

	public FutureStatus(String pluginId, String message, IProgressMonitor progressMonitor) {
		super(IStatus.OK, pluginId, message);
		this.progressMonitor = new FutureStatusProgressMonitor((progressMonitor == null) ? new NullProgressMonitor() : progressMonitor);
		setSeverity(IFutureStatus.IN_PROGRESS);
	}

	public FutureStatus(String message, IProgressMonitor progressMonitor) {
		this(ECFPlugin.PLUGIN_ID, message, progressMonitor);
	}

	public FutureStatus(IProgressMonitor progressMonitor) {
		this(null, progressMonitor);
	}

	public FutureStatus() {
		this(null);
	}

	private void throwIfCanceled() throws CanceledException {
		IProgressMonitor pm = getProgressMonitor();
		if (pm != null && pm.isCanceled())
			throw new CanceledException(this);
	}

	public synchronized Object get() throws InterruptedException, CanceledException {
		throwIfCanceled();
		while (!resultReady)
			wait();
		throwIfCanceled();
		return resultValue;
	}

	public synchronized Object get(long waitTimeInMillis) throws InterruptedException, TimeoutException, CanceledException {
		long startTime = (waitTimeInMillis <= 0) ? 0 : System.currentTimeMillis();
		long waitTime = waitTimeInMillis;
		throwIfCanceled();
		if (resultReady)
			return resultValue;
		else if (waitTime <= 0)
			throw new TimeoutException(waitTimeInMillis);
		else {
			for (;;) {
				wait(waitTime);
				throwIfCanceled();
				if (resultReady)
					return resultValue;
				waitTime = waitTimeInMillis - (System.currentTimeMillis() - startTime);
				if (waitTime <= 0)
					throw new TimeoutException(waitTimeInMillis);
			}
		}
	}

	public IProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	public synchronized boolean isDone() {
		return resultReady;
	}

	synchronized void setCanceled() {
		resultReady = true;
		notifyAll();
	}

	/**
	 * Set the underlying function call that will return a result asynchronously
	 * 
	 * @param function
	 *            the {@link IFutureCallable} to be called
	 * @return Runnable to run in separate thread
	 */
	public Runnable setter(final IFutureCallable function) {
		return new Runnable() {
			public void run() {
				try {
					set(function.call(getProgressMonitor()));
				} catch (Throwable ex) {
					setException(ex);
				}
			}
		};
	}

	protected synchronized void setSeverity(int severity) {
		Assert.isTrue(severity == IStatus.OK || severity == IStatus.CANCEL || severity == IStatus.ERROR || severity == IStatus.INFO || severity == IStatus.WARNING || severity == IFutureStatus.IN_PROGRESS);
		this.futureSeverity = severity;
	}

	public synchronized int getSeverity() {
		return futureSeverity;
	}

	protected synchronized void setException(Throwable ex) {
		super.setException(ex);
		resultReady = true;
		setSeverity(IStatus.ERROR);
		notifyAll();
	}

	synchronized void set(Object newValue) {
		resultValue = newValue;
		resultReady = true;
		setSeverity(IStatus.OK);
		notifyAll();
	}

}
