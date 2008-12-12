/*******************************************************************************
 * Copyright (c) 2008 Versant Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.remoteservices.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.internal.remoteservices.ui.RemoteServiceHandlerUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

public class ConnectRemoteServicehandler extends AbstractHandler {

	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final ID createConnectId = RemoteServiceHandlerUtil.getActiveConnectIDChecked(event);
		// decouple the long running connect call from the ui thread
		Job job = new Job(NLS.bind("Connecting {0}", createConnectId.getName())) {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					final IContainer container = RemoteServiceHandlerUtil.getActiveIRemoteServiceContainerChecked(event);
					container.connect(createConnectId, null);
				} catch (ContainerConnectException e) {
					showException(e);
					return Status.CANCEL_STATUS;
				} catch (ExecutionException e) {
					showException(e);
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
		return null;
	}

	private void showException(final Throwable t) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				String msg = t.toString();
				if (t.getCause() != null) {
					msg += t.getCause().toString();
				}
				MessageDialog.openError(null, t.getLocalizedMessage(), NLS.bind("Exception: {0}", msg));
			}
		});
	}
}
