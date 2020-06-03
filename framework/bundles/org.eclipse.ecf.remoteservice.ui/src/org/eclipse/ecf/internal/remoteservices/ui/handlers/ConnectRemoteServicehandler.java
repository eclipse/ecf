/****************************************************************************
 * Copyright (c) 2008 Versant Corp.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.remoteservices.ui.handlers;

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
import org.eclipse.osgi.util.NLS;

public class ConnectRemoteServicehandler extends ConnectionHandler {

	protected Job getJob(final ExecutionEvent event) throws ExecutionException {
		final ID createConnectId = RemoteServiceHandlerUtil.getActiveConnectIDChecked(event);
		final IContainer container = RemoteServiceHandlerUtil.getActiveIRemoteServiceContainerChecked(event);
		// decouple the long running connect call from the ui thread
		return new Job(NLS.bind("Connecting {0}", createConnectId.getName())) { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				try {
					if (container != null)
						container.connect(createConnectId, null);
				} catch (ContainerConnectException e) {
					showException(e);
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		};
	}
}
