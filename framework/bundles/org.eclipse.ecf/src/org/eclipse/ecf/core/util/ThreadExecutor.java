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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;

public class ThreadExecutor implements IExecutor {

	protected Thread thread;
	protected boolean started;

	public ThreadExecutor() {
		// nothing
	}

	public Thread getThread() {
		return thread;
	}

	protected String createThreadName(IProgressRunnable runnable) {
		return NLS.bind("ThreadExecutor for {0}", runnable.toString()); //$NON-NLS-1$
	}

	protected Runnable createRunnable(final SingleOperationFuture sof, final IProgressRunnable progressRunnable) {
		return new Runnable() {
			public void run() {
				try {
					started = true;
					sof.set(progressRunnable.run(sof.getProgressMonitor()));
				} catch (Throwable t) {
					sof.setException(t);
				}
			}
		};
	}

	public synchronized IFuture execute(IProgressRunnable runnable, IProgressMonitor monitor) throws IllegalThreadStateException {
		Assert.isNotNull(runnable);
		if (thread != null)
			throw new IllegalThreadStateException("Thread for this executor already created"); //$NON-NLS-1$
		// Now create SingleOperationFuture
		SingleOperationFuture sof = new SingleOperationFuture(monitor);
		// Create the thread for this operation
		this.thread = new Thread(createRunnable(sof, runnable), createThreadName(runnable));
		this.thread.setDaemon(true);
		// start thread
		this.thread.start();
		return sof;
	}

	public synchronized boolean hasStarted() {
		return started;
	}

	public synchronized boolean isDone() {
		return (thread != null && !thread.isAlive());
	}
}
