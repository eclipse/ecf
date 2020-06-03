/****************************************************************************
 * Copyright (c) 2008 EclipseSource, IBM, and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *   IBM Corporation - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.core.jobs;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.concurrent.future.*;

/**
 * @since 3.2
 */
public class JobsExecutor extends AbstractExecutor {

	protected int fJobCounter = 1;
	protected String fExecutorName;
	protected boolean fSystem;
	protected ISchedulingRule fSchedulingRule;
	protected long delay;

	public JobsExecutor(String executorName) {
		this(executorName, false);
	}

	public JobsExecutor(String executorName, boolean system) {
		this(executorName, system, null);
	}

	public JobsExecutor(String executorName, boolean system, ISchedulingRule schedulingRule) {
		this(executorName, system, schedulingRule, 0L);
	}

	public JobsExecutor(String executorName, boolean system, ISchedulingRule schedulingRule, long delay) {
		this.fExecutorName = executorName;
		this.fSystem = system;
		this.fSchedulingRule = schedulingRule;
		this.delay = delay;
	}

	protected void setChildProgressMonitor(IProgressMonitor parent, IProgressMonitor child) {
		if (parent instanceof FutureProgressMonitor) {
			((FutureProgressMonitor) parent).setChildProgressMonitor(child);
		}
	}

	protected void safeRun(ISafeProgressRunner runner, IProgressRunnable progressRunnable) {
		runner.runWithProgress(progressRunnable);
	}

	protected String createJobName(String executorName, int jobCounter, IProgressRunnable runnable) {
		return "JobsExecutor(" + executorName + ")." + jobCounter; //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected AbstractFuture createFuture(IProgressMonitor progressMonitor) {
		return new SingleOperationFuture(progressMonitor);
	}

	public IFuture execute(final IProgressRunnable runnable, final IProgressMonitor clientProgressMonitor) {
		Assert.isNotNull(runnable);
		final AbstractFuture sof = createFuture(clientProgressMonitor);
		Job job = new Job(createJobName(fExecutorName, fJobCounter++, runnable)) {
			{
				setSystem(fSystem);
				setRule(fSchedulingRule);
			}

			protected IStatus run(IProgressMonitor monitor) {
				// First check to make sure things haven't been canceled
				if (sof.isCanceled())
					return sof.getStatus();
				// Now add progress monitor as child of future progress monitor
				setChildProgressMonitor(sof.getProgressMonitor(), monitor);
				// Now run safely
				safeRun(sof, runnable);
				return sof.getStatus();
			}
		};
		// Configure job before scheduling
		configureJobForExecution(job);
		job.schedule(delay);
		return sof;
	}

	protected void configureJobForExecution(Job job) {
		// do nothing by default
	}

}
