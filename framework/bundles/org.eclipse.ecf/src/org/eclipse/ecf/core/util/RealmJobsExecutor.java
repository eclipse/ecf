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
package org.eclipse.ecf.core.util;

import java.util.*;
import org.eclipse.core.runtime.jobs.*;

public class RealmJobsExecutor extends JobsExecutor implements IExecutor, IRunnableExecutor {

	Set fJobsInRealm = Collections.synchronizedSet(new HashSet());

	private IJobChangeListener jobChangeListener = new JobChangeAdapter() {
		public void running(IJobChangeEvent event) {
			super.running(event);
			fJobsInRealm.add(event.getJob());
		}

		public void done(IJobChangeEvent event) {
			super.done(event);
			fJobsInRealm.remove(event.getJob());
		}
	};

	public RealmJobsExecutor(String executorName, boolean system, ISchedulingRule schedulingRule) {
		super(executorName, system, schedulingRule);
	}

	protected void configureJobForExecution(Job job) {
		super.configureJobForExecution(job);
		// Add our job change listener to this job, so
		// that we can add/remove jobs from our realm
		job.addJobChangeListener(jobChangeListener);
	}

	public boolean currentJobInRealm() {
		synchronized (fJobsInRealm) {
			Job currentJob = Job.getJobManager().currentJob();
			if (currentJob != null) {
				for (Iterator itr = fJobsInRealm.iterator(); itr.hasNext();) {
					if (currentJob.equals(itr.next())) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
