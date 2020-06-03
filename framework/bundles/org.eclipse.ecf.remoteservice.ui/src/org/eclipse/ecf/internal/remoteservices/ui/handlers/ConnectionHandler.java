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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

public abstract class ConnectionHandler extends AbstractHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
	 * ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Job job = getJob(event);
		job.setUser(true);
		job.schedule();
		return null;
	}

	protected abstract Job getJob(ExecutionEvent event) throws ExecutionException;

	protected void showException(final Throwable t) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				String msg = t.toString();
				if (t.getCause() != null) {
					msg += t.getCause().toString();
				}
				MessageDialog.openError(null, t.getLocalizedMessage(), NLS.bind("Exception: {0}", msg)); //$NON-NLS-1$
			}
		});
	}
}
