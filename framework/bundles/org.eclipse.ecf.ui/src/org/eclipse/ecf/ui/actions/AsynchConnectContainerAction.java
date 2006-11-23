/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.ui.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.IExceptionHandler;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.ui.Activator;
import org.eclipse.ecf.internal.ui.UIDebugOptions;
import org.eclipse.jface.action.IAction;

public class AsynchConnectContainerAction extends SynchConnectContainerAction {

	private static final String RUN_METHOD = "run";

	private static final int ERROR_CODE = 999;

	public AsynchConnectContainerAction(IContainer container, ID targetID,
			IConnectContext connectContext, IExceptionHandler exceptionHandler) {
		super(container, targetID, connectContext, exceptionHandler);
	}

	public AsynchConnectContainerAction(IContainer container, ID targetID,
			IConnectContext connectContext) {
		this(container, targetID, connectContext, null);
	}

	public void dispose() {
		this.container = null;
		this.targetID = null;
		this.connectContext = null;
		this.window = null;
	}

	protected IStatus handleException(Throwable e) {
		if (exceptionHandler != null)
			return exceptionHandler.handleException(e);
		else if (e == null)
			return Status.OK_STATUS;
		else
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, ERROR_CODE,
					"Exception in run", e);
	}

	class ContainerMutex implements ISchedulingRule {

		IContainer container;

		public ContainerMutex(IContainer container) {
			this.container = container;
		}

		protected boolean isSameContainer(IContainer other) {
			if (other != null && container.getID().equals(other.getID()))
				return true;
			return false;
		}

		protected IContainer getContainer() {
			return AsynchConnectContainerAction.this.getContainer();
		}

		public boolean isConflicting(ISchedulingRule rule) {
			if (rule == this
					|| isSameContainer(((ContainerMutex) rule).getContainer()))
				return true;
			else
				return false;
		}

		public boolean contains(ISchedulingRule rule) {
			return isConflicting(rule);
		}
	}

	protected class AsynchActionJob extends Job {

		public AsynchActionJob() {
			super("AsynchConnectContainerAction." + getContainer().getID());
			setRule(new ContainerMutex(getContainer()));
		}

		protected IStatus run(IProgressMonitor monitor) {
			Trace.entering(Activator.getDefault(),
					UIDebugOptions.METHODS_ENTERING, this.getClass(),
					RUN_METHOD);
			monitor.beginTask("Connecting", 100);
			monitor.worked(10);
			try {
				container.connect(targetID, connectContext);
				IStatus status = Status.OK_STATUS;
				if (monitor.isCanceled()) {
					container.disconnect();
					status = Status.CANCEL_STATUS;
					return status;
				}
				monitor.worked(90);
				return status;
			} catch (ContainerConnectException e) {
				Trace.catching(Activator.getDefault(),
						UIDebugOptions.METHODS_ENTERING, this.getClass(),
						RUN_METHOD, e);
				IStatus status = handleException(e);
				return status;
			} finally {
				monitor.done();
				Trace.exiting(Activator.getDefault(),
						UIDebugOptions.METHODS_EXITING, this.getClass(),
						RUN_METHOD);
			}
		}

	}

	public void run(IAction action) {
		new AsynchActionJob().schedule();
	}

}
