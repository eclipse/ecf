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

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ecf.filetransfer.*;
import org.eclipse.ecf.filetransfer.events.IRemoteFileSystemDirectoryEvent;
import org.eclipse.ecf.filetransfer.events.IRemoteFileSystemEvent;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.internal.provider.filetransfer.Messages;
import org.eclipse.osgi.util.NLS;

/**
 *
 */
public class FileSystemBrowser {

	IFileID directoryID = null;
	URL directoryURL = null;
	IRemoteFileSystemListener listener = null;
	File localDirectory;

	Job job = null;
	Exception exception = null;
	IRemoteFile[] remoteFiles = null;

	Object lock = new Object();

	/**
	 * @param listener 
	 * @param url 
	 * @param directoryID2 
	 */
	public FileSystemBrowser(IFileID directoryID2, URL url, IRemoteFileSystemListener listener) {
		Assert.isNotNull(directoryID2);
		this.directoryID = directoryID2;
		Assert.isNotNull(url);
		this.directoryURL = url;
		Assert.isNotNull(listener);
		this.listener = listener;
	}

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
				FileSystemBrowser.this.exception = e;
			} finally {
				listener.handleRemoteFileEvent(createRemoteFileEvent());
				cleanUp();
			}
			return Status.OK_STATUS;
		}

	}

	void cleanUp() {
		synchronized (lock) {
			job = null;
		}
	}

	protected void runDirectoryRequest() throws Exception {
		File[] files = localDirectory.listFiles();
		remoteFiles = new LocalFile[files.length];
		for (int i = 0; i < files.length; i++) {
			remoteFiles[i] = new LocalFile(files[i]);
		}
	}

	public IRemoteFileSystemRequest sendDirectoryRequest() throws RemoteFileSystemException {
		localDirectory = new File(directoryURL.getPath());
		if (!localDirectory.exists())
			throw new RemoteFileSystemException(NLS.bind(Messages.FileSystemBrowser_EXCEPTION_DIRECTORY_DOES_NOT_EXIST, localDirectory));
		if (!localDirectory.isDirectory())
			throw new RemoteFileSystemException(NLS.bind(Messages.FileSystemBrowser_EXCEPTION_NOT_DIRECTORY, localDirectory));

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
	IRemoteFileSystemEvent createRemoteFileEvent() {
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
