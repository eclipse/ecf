/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.provider.filetransfer.retrieve;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.filetransfer.IFileTransferListener;
import org.eclipse.ecf.filetransfer.IFileTransferPausable;
import org.eclipse.ecf.filetransfer.IIncomingFileTransfer;
import org.eclipse.ecf.filetransfer.IRetrieveFileTransferContainerAdapter;
import org.eclipse.ecf.filetransfer.IncomingFileTransferException;
import org.eclipse.ecf.filetransfer.UserCancelledException;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveDataEvent;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveDoneEvent;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.internal.provider.filetransfer.Activator;
import org.eclipse.ecf.internal.provider.filetransfer.identity.FileTransferNamespace;

public abstract class AbstractRetrieveFileTransfer implements IIncomingFileTransfer,
		IRetrieveFileTransferContainerAdapter, IFileTransferPausable {

	public static final int DEFAULT_BUF_LENGTH = 4096;

	private static final int FILETRANSFER_ERRORCODE = 1001;

	protected Job job;

	protected URL remoteFileURL;

	protected IFileID remoteFileID;

	protected IFileTransferListener listener;

	protected int buff_length = DEFAULT_BUF_LENGTH;

	protected boolean done = false;

	protected long bytesReceived = 0;

	protected InputStream remoteFileContents;

	protected OutputStream localFileContents;
	
	protected boolean closeOutputStream = true;

	protected Exception exception;

	protected long fileLength = -1;

	protected URL getRemoteFileURL() {
		return remoteFileURL;
	}

	protected void setInputStream(InputStream ins) {
		remoteFileContents = ins;
	}

	protected void setOutputStream(OutputStream outs) {
		localFileContents = outs;
	}

	protected void setCloseOutputStream(boolean close) {
		closeOutputStream = close;
	}

	protected void setFileLength(long length) {
		fileLength = length;
	}

	public AbstractRetrieveFileTransfer() {
	}

	class FileTransferJob extends Job {

		public FileTransferJob(String name) {
			super(name);
		}

		protected IStatus run(IProgressMonitor monitor) {
			byte[] buf = new byte[buff_length];
			int totalWork = ((fileLength == -1) ? 100 : (int) fileLength);
			monitor.beginTask(getRemoteFileURL().toString() + " - data ",
					totalWork);
			try {
				while (!isDone()) {
					if (monitor.isCanceled())
						throw new UserCancelledException("cancelled by user");
					int bytes = remoteFileContents.read(buf);
					if (bytes != -1) {
						bytesReceived += bytes;
						localFileContents.write(buf, 0, bytes);
						fireTransferReceiveDataEvent();
						monitor.worked(bytes);
					} else
						done = true;
				}
			} catch (Exception e) {
				exception = e;
				done = true;
			} finally {
				hardClose();
				monitor.done();
				fireTransferReceiveDoneEvent();
			}
			return getFinalStatus(exception);
		}

	}

	protected IStatus getFinalStatus(Throwable exception) {
		if (exception == null)
			return new Status(IStatus.OK, Activator.getDefault().getBundle()
					.getSymbolicName(), 0, "Transfer Completed OK", null);
		else
			return new Status(IStatus.ERROR, Activator.getDefault().getBundle()
					.getSymbolicName(), FILETRANSFER_ERRORCODE,
					"Transfer Exception", exception);
	}

	protected void hardClose() {
		try {
			if (remoteFileContents != null) remoteFileContents.close();
		} catch (IOException e) {
		}
		try {
			if (closeOutputStream && localFileContents != null)
				localFileContents.close();
		} catch (IOException e) {
		}
		job = null;
		remoteFileContents = null;
		localFileContents = null;
	}

	protected void fireTransferReceiveDoneEvent() {
		listener
				.handleTransferEvent(new IIncomingFileTransferReceiveDoneEvent() {

					private static final long serialVersionUID = 6925524078226825710L;

					public IIncomingFileTransfer getSource() {
						return AbstractRetrieveFileTransfer.this;
					}

					public Exception getException() {
						return AbstractRetrieveFileTransfer.this.getException();
					}

					public String toString() {
						StringBuffer sb = new StringBuffer(
								"IIncomingFileTransferReceiveDoneEvent[");
						sb.append("isDone=").append(done).append(";");
						sb.append("bytesReceived=").append(bytesReceived)
								.append("]");
						return sb.toString();
					}
				});
	}

	protected void fireTransferReceiveDataEvent() {
		listener
				.handleTransferEvent(new IIncomingFileTransferReceiveDataEvent() {
					private static final long serialVersionUID = -5656328374614130161L;

					public IIncomingFileTransfer getSource() {
						return AbstractRetrieveFileTransfer.this;
					}

					public String toString() {
						StringBuffer sb = new StringBuffer(
								"IIncomingFileTransferReceiveDataEvent[");
						sb.append("isDone=").append(done).append(";");
						sb.append("bytesReceived=").append(bytesReceived)
								.append(";");
						sb.append("percentComplete=").append(
								getPercentComplete() * 100).append("]");
						return sb.toString();
					}
				});
	}

	public long getBytesReceived() {
		return bytesReceived;
	}

	public void cancel() {
		job.cancel();
	}

	public Exception getException() {
		return exception;
	}

	public double getPercentComplete() {
		return ((double) bytesReceived / (double) fileLength);
	}

	public boolean isDone() {
		return done;
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

	/**
	 * Open incoming and outgoing streams associated with this file transfer.
	 * 
	 * @throws IncomingFileTransferException
	 */
	protected abstract void openStreams(Map options) throws IncomingFileTransferException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.filetransfer.IRetrieveFileTransferContainerAdapter#sendRetrieveRequest(org.eclipse.ecf.filetransfer.identity.IFileID,
	 *      org.eclipse.ecf.filetransfer.IFileTransferListener, java.util.Map)
	 */
	public void sendRetrieveRequest(final IFileID remoteFileID,
			IFileTransferListener transferListener, Map options)
			throws IncomingFileTransferException {
		Assert.isNotNull(remoteFileID,"remoteFileID cannot be null");
		Assert.isNotNull(transferListener,"transferListener cannot be null");
		done = false;
		bytesReceived = 0;
		exception = null;
		fileLength = 0;
		
		try {
			this.remoteFileID = remoteFileID;
			this.remoteFileURL = new URL(remoteFileID.getName());
		} catch (MalformedURLException e) {
			throw new IncomingFileTransferException(
					"Exception creating URI for " + remoteFileID, e);
		}
		this.listener = transferListener;
		openStreams(options);
	}

	public Namespace getRetrieveNamespace() {
		return IDFactory.getDefault().getNamespaceByName(
				FileTransferNamespace.PROTOCOL);
	}

	public boolean isPaused() {
		if (job == null)
			return false;
		else
			synchronized (job) {
				if (job.getState() == Job.SLEEPING)
					return true;
				return false;
			}
	}

	public boolean pause() {
		if (job == null)
			return false;
		else
			synchronized (job) {
				return job.sleep();
			}
	}

	public boolean resume() {
		if (job == null)
			return false;
		else
			synchronized (job) {
				if (job.getState() == Job.SLEEPING) {
					job.wakeUp();
					return true;
				} else
					return false;
			}
	}

	public abstract boolean supportsProtocol(String protocolString);
	
}
