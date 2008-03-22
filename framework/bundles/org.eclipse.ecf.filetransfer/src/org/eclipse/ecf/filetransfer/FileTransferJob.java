/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:  Contributors: Cloudsmith, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.filetransfer;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveStartEvent;
import org.eclipse.ecf.internal.filetransfer.Messages;

/**
 * {@link Job} subclass for executing file transfers.  This class should
 * be subclassed to create a customized {@link Job} for passing to
 * an incoming or outgoing file transfer.  For example, to use a custom
 * job for doing a file transfer retrieval via {@link IIncomingFileTransferReceiveStartEvent#receive(java.io.File, FileTransferJob)}:
 * <pre>
 * class MyFileTransferJob extends FileTransferJob {
 * 		public MyFileTransferJob(String name) {
 * 			super(name);
 * 		}
 * 
 * 		public boolean belongsTo(Object o) {
 * 			// insert own logic to decide whether
 *  		// this file transfer job should be part
 *  		// of a group
 *  		//
 * 		}
 * }
 * 
 * MyFileTransferJob myJob = new MyFileTransferJob("myname");
 * incomingfiletransfer = event.receive(outputstream,myJob);
 * </pre>
 * @since 2.0
 */
public class FileTransferJob extends Job {

	private IFileTransferRunnable fileTransferRunnable;

	/**
	 * @param name the name for this file transfer job.  Should not be <code>null</code>.
	 */
	public FileTransferJob(String name) {
		super(name);
	}

	public final void setFileTransferRunnable(IFileTransferRunnable fileTransferRunnable) {
		this.fileTransferRunnable = fileTransferRunnable;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected final IStatus run(IProgressMonitor mntr) {
		if (this.fileTransferRunnable == null)
			return new Status(IStatus.ERROR, org.eclipse.ecf.internal.filetransfer.Activator.PLUGIN_ID, IStatus.ERROR, Messages.FileTransferJob_STATUSERROR_NO_RUNNABLE, null);
		return this.fileTransferRunnable.performFileTransfer(mntr);
	}
}
