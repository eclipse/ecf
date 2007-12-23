/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.provider.filetransfer.browse;

import java.util.Arrays;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ecf.filetransfer.*;
import org.eclipse.ecf.filetransfer.events.IRemoteFileSystemDirectoryEvent;
import org.eclipse.ecf.filetransfer.events.IRemoteFileSystemEvent;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.internal.provider.filetransfer.Messages;

/**
 * Abstract class for browsing an efs file system.
 */
public abstract class AbstractFileSystemBrowser {

	protected IFileID directoryID = null;
	protected IRemoteFileSystemListener listener = null;

	Job job = null;
	protected Exception exception = null;
	protected IRemoteFile[] remoteFiles = null;

	Object lock = new Object();

	class DirectoryJob extends Job {

		public DirectoryJob() {
			super(directoryID.getName());
		}

		protected IStatus run(IProgressMonitor monitor) {
			try {
				if (monitor.isCanceled())
					throw new UserCancelledException(Messages.AbstractRetrieveFileTransfer_Exception_User_Cancelled);
				runDirectoryRequest();
			} catch (Exception e) {
				AbstractFileSystemBrowser.this.exception = e;
			} finally {
				listener.handleRemoteFileEvent(createRemoteFileEvent());
				cleanUp();
			}
			return Status.OK_STATUS;
		}

	}

	protected void cleanUp() {
		synchronized (lock) {
			job = null;
		}
	}

	/**
	 * Run the actual directory request.  This method is called within the job created to actually get the
	 * directory information.
	 * @throws Exception if some problem with making the request or receiving response to the request.
	 */
	protected abstract void runDirectoryRequest() throws Exception;

	public AbstractFileSystemBrowser(IFileID directoryID2, IRemoteFileSystemListener listener) {
		Assert.isNotNull(directoryID2);
		this.directoryID = directoryID2;
		Assert.isNotNull(listener);
		this.listener = listener;
	}

	public IRemoteFileSystemRequest sendDirectoryRequest() {
		job = new DirectoryJob();
		job.schedule();
		return new IRemoteFileSystemRequest() {

			public void cancel() {
				synchronized (lock) {
					if (job != null)
						job.cancel();
				}
			}

			public IFileID getDirectoryID() {
				return directoryID;
			}

			public IRemoteFileSystemListener getRemoteFileListener() {
				return listener;
			}

		};

	}

	/**
	 * @return file system directory event
	 */
	protected IRemoteFileSystemEvent createRemoteFileEvent() {
		return new IRemoteFileSystemDirectoryEvent() {

			public IFileID getDirectory() {
				return directoryID;
			}

			public Exception getException() {
				return exception;
			}

			public String toString() {
				StringBuffer buf = new StringBuffer("RemoteFileSystemDirectoryEvent["); //$NON-NLS-1$
				buf.append("directoryID=").append(directoryID).append(";"); //$NON-NLS-1$ //$NON-NLS-2$
				buf.append("files=" + Arrays.asList(remoteFiles)).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
				return buf.toString();
			}

			public IRemoteFile[] getRemoteFiles() {
				return remoteFiles;
			}
		};
	}

}
