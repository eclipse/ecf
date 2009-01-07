/*******************************************************************************
* Copyright (c) 2008 EclipseSource, IBM, and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
*   IBM Corporation - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.provider.remoteservice.generic;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ecf.core.util.*;
import org.eclipse.osgi.util.NLS;

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
		runner.safeRun(progressRunnable);
	}

	protected String createJobName(String executorName, int jobCounter, IProgressRunnable runnable) {
		return NLS.bind("Executor({0}).{1}", executorName, Integer.toString(jobCounter)); //$NON-NLS-1$
	}

	public IFuture execute(final IProgressRunnable runnable, final IProgressMonitor clientProgressMonitor) {
		Assert.isNotNull(runnable);
		final SingleOperationFuture sof = new SingleOperationFuture(clientProgressMonitor);
		Job job = new Job(createJobName(fExecutorName, fJobCounter++, runnable)) {
			{
				setSystem(fSystem);
				setRule(fSchedulingRule);
			}

			protected IStatus run(IProgressMonitor monitor) {
				IProgressMonitor pm = sof.getProgressMonitor();
				// First check to make sure things haven't been canceled
				if (pm.isCanceled())
					return sof.getStatus();
				// Now add progress monitor as child of future progress monitor
				setChildProgressMonitor(pm, monitor);
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
