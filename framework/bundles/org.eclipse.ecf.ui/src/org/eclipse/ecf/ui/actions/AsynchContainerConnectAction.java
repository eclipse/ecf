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
import org.eclipse.ecf.internal.ui.Activator;
import org.eclipse.ecf.internal.ui.Messages;
import org.eclipse.jface.action.IAction;
import org.eclipse.osgi.util.NLS;

/**
 * Action class for invoking {@link IContainer#connect(ID, IConnectContext)} as
 * separate job.
 */
public class AsynchContainerConnectAction extends SynchContainerConnectAction {

	public AsynchContainerConnectAction(IContainer container, ID targetID,
			IConnectContext connectContext, IExceptionHandler exceptionHandler) {
		super(container, targetID, connectContext, exceptionHandler);
	}

	public AsynchContainerConnectAction(IContainer container, ID targetID,
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
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					IStatus.ERROR,
					Messages.AsynchContainerConnectAction_EXCEPTION_CONNECT, e);
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
			return AsynchContainerConnectAction.this.getContainer();
		}

		public boolean isConflicting(ISchedulingRule rule) {
			if (rule == this)
				return true;
			else if (rule instanceof ContainerMutex
					&& isSameContainer(((ContainerMutex) rule).getContainer()))
				return true;
			else
				return false;
		}

		public boolean contains(ISchedulingRule rule) {
			return (rule == this);
		}
	}

	class AsynchActionJob extends Job {

		public AsynchActionJob() {
			super(NLS.bind(Messages.AsynchContainerConnectAction_JOB_NAME,
					getContainer().getID().getName()));
			setRule(new ContainerMutex(getContainer()));
		}

		public IStatus run(IProgressMonitor monitor) {
			monitor.beginTask(NLS.bind(
					Messages.AsynchContainerConnectAction_MONITOR_BEGIN_TASK,
					getContainer().getID().getName()), 100);
			monitor.worked(30);
			try {
				container.connect(targetID, connectContext);
				if (monitor.isCanceled()) {
					container.disconnect();
					return Status.CANCEL_STATUS;
				}
				monitor.worked(60);
				return Status.OK_STATUS;
			} catch (ContainerConnectException e) {
				return handleException(e);
			} finally {
				monitor.done();
			}
		}

	}

	public void run(IAction action) {
		new AsynchActionJob().schedule();
	}

}
