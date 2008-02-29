/*******************************************************************************
 * Copyright (c) 2004, 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *    Benjamin Cabe <benjamin.cabe@anyware-tech.com> - bug 220258
 ******************************************************************************/
package org.eclipse.ecf.provider.filetransfer.retrieve;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Map;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.Proxy;
import org.eclipse.ecf.core.util.ProxyAddress;
import org.eclipse.ecf.filetransfer.*;
import org.eclipse.ecf.filetransfer.events.*;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransfer;
import org.eclipse.ecf.internal.provider.filetransfer.Activator;
import org.eclipse.ecf.internal.provider.filetransfer.Messages;
import org.eclipse.ecf.provider.filetransfer.identity.FileTransferNamespace;
import org.eclipse.osgi.util.NLS;

public abstract class AbstractRetrieveFileTransfer implements IIncomingFileTransfer, IRetrieveFileTransfer, IFileTransferPausable {

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

	protected Map options = null;

	protected boolean paused = false;

	protected IFileRangeSpecification rangeSpecification = null;

	protected Proxy proxy;

	protected IConnectContext connectContext;

	protected long transferStartTime;

	protected double downloadRateBytesPerSecond = 0L;

	public AbstractRetrieveFileTransfer() {
		//
	}

	private IFileTransferRunnable fileTransferRunnable = new IFileTransferRunnable() {
		public IStatus performFileTransfer(IProgressMonitor monitor) {
			transferStartTime = System.currentTimeMillis();
			final byte[] buf = new byte[buff_length];
			final long totalWork = ((fileLength == -1) ? 100 : fileLength);
			double factor = (totalWork > Integer.MAX_VALUE) ? (((double) Integer.MAX_VALUE) / ((double) totalWork)) : 1.0;
			int work = (totalWork > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) totalWork;
			monitor.beginTask(getRemoteFileURL().toString() + Messages.AbstractRetrieveFileTransfer_Progress_Data, work);
			try {
				while (!isDone() && !isPaused()) {
					if (monitor.isCanceled())
						throw new UserCancelledException(Messages.AbstractRetrieveFileTransfer_Exception_User_Cancelled);
					final int bytes = remoteFileContents.read(buf);
					handleReceivedData(buf, bytes, factor, monitor);
				}
			} catch (final Exception e) {
				exception = e;
				done = true;
			} finally {
				hardClose();
				monitor.done();
				try {
					if (isPaused())
						fireTransferReceivePausedEvent();
					else
						fireTransferReceiveDoneEvent();
				} catch (Exception e) {
					// simply log
					Activator.getDefault().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, Messages.AbstractRetrieveFileTransfer_EXCEPTION_IN_FINALLY, e));
				}
			}
			return getFinalStatus(exception);
		}

	};

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

	protected Map getOptions() {
		return options;
	}

	protected void handleReceivedData(byte[] buf, int bytes, double factor, IProgressMonitor monitor) throws IOException {
		if (bytes != -1) {
			bytesReceived += bytes;
			localFileContents.write(buf, 0, bytes);
			downloadRateBytesPerSecond = (bytesReceived / ((System.currentTimeMillis() + 1 - transferStartTime) / 1000.0));
			monitor.setTaskName(createJobName() + Messages.AbstractRetrieveFileTransfer_Progress_Data + NLS.bind(Messages.AbstractRetrieveFileTransfer_InfoTransferRate, toHumanReadableBytes(downloadRateBytesPerSecond)));
			monitor.worked((int) Math.round(factor * bytes));
			fireTransferReceiveDataEvent();
		} else
			done = true;
	}

	public static String toHumanReadableBytes(double size) {
		double convertedSize;
		String unit;

		if (size / (1024 * 1024 * 1024) >= 1) {
			convertedSize = size / (1024 * 1024 * 1024);
			unit = Messages.AbstractRetrieveFileTransfer_SizeUnitGB;
		} else if (size / (1024 * 1024) >= 1) {
			convertedSize = size / (1024 * 1024);
			unit = Messages.AbstractRetrieveFileTransfer_SizeUnitMB;
		} else if (size / 1024 >= 1) {
			convertedSize = size / 1024;
			unit = Messages.AbstractRetrieveFileTransfer_SizeUnitKB;
		} else {
			convertedSize = size;
			unit = Messages.AbstractRetrieveFileTransfer_SizeUnitBytes;
		}

		DecimalFormat df = new DecimalFormat(NLS.bind(Messages.AbstractRetrieveFileTransfer_TransferRateFormat, unit));
		return df.format(convertedSize);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.IIdentifiable#getID()
	 */
	public ID getID() {
		return remoteFileID;
	}

	protected IStatus getFinalStatus(Throwable exception1) {
		if (exception1 == null)
			return new Status(IStatus.OK, Activator.getDefault().getBundle().getSymbolicName(), 0, Messages.AbstractRetrieveFileTransfer_Status_Transfer_Completed_OK, null);
		else if (exception1 instanceof UserCancelledException)
			return new Status(IStatus.CANCEL, Activator.PLUGIN_ID, FILETRANSFER_ERRORCODE, Messages.AbstractRetrieveFileTransfer_Exception_User_Cancelled, exception1);
		else
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, FILETRANSFER_ERRORCODE, Messages.AbstractRetrieveFileTransfer_Status_Transfer_Exception, exception1);
	}

	protected void hardClose() {
		try {
			if (remoteFileContents != null)
				remoteFileContents.close();
		} catch (final IOException e) {
			Activator.getDefault().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, "hardClose", e)); //$NON-NLS-1$
		}
		try {
			if (localFileContents != null && closeOutputStream)
				localFileContents.close();
		} catch (final IOException e) {
			Activator.getDefault().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, "hardClose", e)); //$NON-NLS-1$
		}
		job = null;
		remoteFileContents = null;
		localFileContents = null;
	}

	protected void fireTransferReceivePausedEvent() {
		listener.handleTransferEvent(new IIncomingFileTransferReceivePausedEvent() {

			private static final long serialVersionUID = -1317411290525985140L;

			public IIncomingFileTransfer getSource() {
				return AbstractRetrieveFileTransfer.this;
			}

			public String toString() {
				final StringBuffer sb = new StringBuffer("IIncomingFileTransferReceivePausedEvent["); //$NON-NLS-1$
				sb.append("bytesReceived=").append(bytesReceived) //$NON-NLS-1$
						.append(";fileLength=").append(fileLength).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
				return sb.toString();
			}
		});
	}

	protected void fireTransferReceiveDoneEvent() {
		listener.handleTransferEvent(new IIncomingFileTransferReceiveDoneEvent() {

			private static final long serialVersionUID = 6925524078226825710L;

			public IIncomingFileTransfer getSource() {
				return AbstractRetrieveFileTransfer.this;
			}

			public Exception getException() {
				return AbstractRetrieveFileTransfer.this.getException();
			}

			public String toString() {
				final StringBuffer sb = new StringBuffer("IIncomingFileTransferReceiveDoneEvent["); //$NON-NLS-1$
				sb.append("bytesReceived=").append(bytesReceived) //$NON-NLS-1$
						.append(";fileLength=").append(fileLength).append(";exception=").append(getException()) //$NON-NLS-1$ //$NON-NLS-2$
						.append("]"); //$NON-NLS-1$
				return sb.toString();
			}
		});
	}

	protected void fireTransferReceiveDataEvent() {
		listener.handleTransferEvent(new IIncomingFileTransferReceiveDataEvent() {
			private static final long serialVersionUID = -5656328374614130161L;

			public IIncomingFileTransfer getSource() {
				return AbstractRetrieveFileTransfer.this;
			}

			public String toString() {
				final StringBuffer sb = new StringBuffer("IIncomingFileTransferReceiveDataEvent["); //$NON-NLS-1$
				sb.append("bytesReceived=").append(bytesReceived) //$NON-NLS-1$
						.append(";fileLength=").append(fileLength) //$NON-NLS-1$ 
						.append("]"); //$NON-NLS-1$
				return sb.toString();
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.IRetrieveFileTransferContainerAdapter#setConnectContextForAuthentication(org.eclipse.ecf.core.security.IConnectContext)
	 */
	public void setConnectContextForAuthentication(IConnectContext connectContext) {
		this.connectContext = connectContext;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.IRetrieveFileTransferContainerAdapter#setProxy(org.eclipse.ecf.core.util.Proxy)
	 */
	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.IIncomingFileTransfer#getBytesReceived()
	 */
	public long getBytesReceived() {
		return bytesReceived;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.IFileTransfer#cancel()
	 */
	public void cancel() {
		if (isPaused()) {
			done = true;
			this.exception = new UserCancelledException(Messages.AbstractRetrieveFileTransfer_Exception_User_Cancelled);
			fireTransferReceiveDoneEvent();
		} else if (job != null)
			job.cancel();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.IFileTransfer#getException()
	 */
	public Exception getException() {
		return exception;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.filetransfer.IFileTransfer#getPercentComplete()
	 */
	public double getPercentComplete() {
		if (fileLength == -1 || fileLength == 0)
			return fileLength;
		return ((double) bytesReceived / (double) fileLength);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.IFileTransfer#getFileLength()
	 */
	public long getFileLength() {
		return fileLength;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.IFileTransfer#isDone()
	 */
	public boolean isDone() {
		return done;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == null)
			return null;
		if (adapter.isInstance(this)) {
			return this;
		}
		final IAdapterManager adapterManager = Activator.getDefault().getAdapterManager();
		return (adapterManager == null) ? null : adapterManager.loadAdapter(this, adapter.getName());
	}

	/**
	 * Open incoming and outgoing streams associated with this file transfer.
	 * Subclasses must implement this method to open input and output streams.
	 * The <code>remoteFileContents</code> and <code>localFileContent</code>
	 * must be non-<code>null</code> after successful completion of the
	 * implementation of this method.
	 * 
	 * @throws IncomingFileTransferException
	 */
	protected abstract void openStreams() throws IncomingFileTransferException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.filetransfer.IRetrieveFileTransferContainerAdapter#sendRetrieveRequest(org.eclipse.ecf.filetransfer.identity.IFileID,
	 *      org.eclipse.ecf.filetransfer.IFileTransferListener, java.util.Map)
	 */
	public void sendRetrieveRequest(final IFileID remoteFileID1, IFileTransferListener transferListener, Map options1) throws IncomingFileTransferException {
		sendRetrieveRequest(remoteFileID1, null, transferListener, options1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.IRetrieveFileTransferContainerAdapter#getRetrieveNamespace()
	 */
	public Namespace getRetrieveNamespace() {
		return IDFactory.getDefault().getNamespaceByName(FileTransferNamespace.PROTOCOL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.filetransfer.IFileTransferPausable#isPaused()
	 */
	public boolean isPaused() {
		return paused;
	}

	/**
	 * Subclass overridable version of {@link #pause()}. Subclasses must
	 * provide an implementation of this method to support
	 * {@link IFileTransferPausable}.
	 * 
	 * @return true if the pause is successful. <code>false</code> otherwise.
	 */
	protected abstract boolean doPause();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.filetransfer.IFileTransferPausable#pause()
	 */
	public boolean pause() {
		return doPause();
	}

	/**
	 * Subclass overridable version of {@link #resume()}. Subclasses must
	 * provide an implementation of this method to support
	 * {@link IFileTransferPausable}.
	 * 
	 * @return true if the resume is successful. <code>false</code> otherwise.
	 */
	protected abstract boolean doResume();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.filetransfer.IFileTransferPausable#resume()
	 */
	public boolean resume() {
		return doResume();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.IIncomingFileTransfer#getListener()
	 */
	public IFileTransferListener getListener() {
		return listener;
	}

	protected String createRangeName() {
		if (rangeSpecification == null)
			return ""; //$NON-NLS-1$
		return "[" + rangeSpecification.getStartPosition() + "," + rangeSpecification.getEndPosition() + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	protected String createJobName() {
		return getRemoteFileURL().toString() + createRangeName();
	}

	protected void setupAndScheduleJob(FileTransferJob fileTransferJob) {
		if (fileTransferJob == null) {
			// Create our own
			fileTransferJob = new FileTransferJob(createJobName());
		}
		// Now set to our runnable
		fileTransferJob.setFileTransferRunnable(fileTransferRunnable);
		job = fileTransferJob;
		job.schedule();
	}

	protected void fireReceiveStartEvent() {
		listener.handleTransferEvent(new IIncomingFileTransferReceiveStartEvent() {
			private static final long serialVersionUID = -59096575294481755L;

			/* (non-Javadoc)
			 * @see org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveStartEvent#getFileID()
			 */
			public IFileID getFileID() {
				return remoteFileID;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveStartEvent#receive(java.io.File)
			 */
			public IIncomingFileTransfer receive(File localFileToSave) throws IOException {
				return receive(localFileToSave, null);
			}

			/* (non-Javadoc)
			 * @see org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveStartEvent#receive(java.io.File, org.eclipse.ecf.filetransfer.FileTransferJob)
			 */
			public IIncomingFileTransfer receive(File localFileToSave, FileTransferJob fileTransferJob) throws IOException {
				setOutputStream(new BufferedOutputStream(new FileOutputStream(localFileToSave)));
				setupAndScheduleJob(fileTransferJob);
				return AbstractRetrieveFileTransfer.this;
			}

			/**
			 * @param streamToStore
			 * @return incoming file transfer instance.
			 * @throws IOException not thrown in this implementation.
			 */
			public IIncomingFileTransfer receive(OutputStream streamToStore) throws IOException {
				return receive(streamToStore, null);
			}

			/**
			 * @throws IOException not actually thrown by this implementation.
			 */
			public IIncomingFileTransfer receive(OutputStream streamToStore, FileTransferJob fileTransferJob) throws IOException {
				setOutputStream(streamToStore);
				setCloseOutputStream(false);
				setupAndScheduleJob(fileTransferJob);
				return AbstractRetrieveFileTransfer.this;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveStartEvent#cancel()
			 */
			public void cancel() {
				hardClose();
			}

			/* (non-Javadoc)
			 * @see java.lang.Object#toString()
			 */
			public String toString() {
				final StringBuffer sb = new StringBuffer("IIncomingFileTransferReceiveStartEvent["); //$NON-NLS-1$
				sb.append("isdone=").append(done).append(";"); //$NON-NLS-1$ //$NON-NLS-2$
				sb.append("bytesReceived=").append(bytesReceived) //$NON-NLS-1$
						.append("]"); //$NON-NLS-1$
				return sb.toString();
			}

		});
	}

	protected void fireReceiveResumedEvent() {
		listener.handleTransferEvent(new IIncomingFileTransferReceiveResumedEvent() {

			private static final long serialVersionUID = 7111739642849612839L;

			/* (non-Javadoc)
			 * @see org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveStartEvent#getFileID()
			 */
			public IFileID getFileID() {
				return remoteFileID;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveStartEvent#receive(java.io.File)
			 */
			public IIncomingFileTransfer receive(File localFileToSave) throws IOException {
				return receive(localFileToSave, null);
			}

			/* (non-Javadoc)
			 * @see org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveStartEvent#receive(java.io.File, org.eclipse.ecf.filetransfer.FileTransferJob)
			 */
			public IIncomingFileTransfer receive(File localFileToSave, FileTransferJob fileTransferJob) throws IOException {
				setOutputStream(new BufferedOutputStream(new FileOutputStream(localFileToSave)));
				setupAndScheduleJob(fileTransferJob);
				return AbstractRetrieveFileTransfer.this;
			}

			/**
			 * @param streamToStore
			 * @return incoming file transfer instance.
			 * @throws IOException not thrown in this implementation.
			 */
			public IIncomingFileTransfer receive(OutputStream streamToStore) throws IOException {
				return receive(streamToStore, null);
			}

			/**
			 * @throws IOException not actually thrown by this implementation.
			 */
			public IIncomingFileTransfer receive(OutputStream streamToStore, FileTransferJob fileTransferJob) throws IOException {
				setOutputStream(streamToStore);
				setCloseOutputStream(false);
				setupAndScheduleJob(fileTransferJob);
				return AbstractRetrieveFileTransfer.this;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveStartEvent#cancel()
			 */
			public void cancel() {
				hardClose();
			}

			public String toString() {
				final StringBuffer sb = new StringBuffer("IIncomingFileTransferReceiveResumedEvent["); //$NON-NLS-1$
				sb.append("isdone=").append(done).append(";"); //$NON-NLS-1$ //$NON-NLS-2$
				sb.append("bytesReceived=").append(bytesReceived) //$NON-NLS-1$
						.append("]"); //$NON-NLS-1$
				return sb.toString();
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.filetransfer.IIncomingFileTransfer#getFileRangeSpecification()
	 */
	public IFileRangeSpecification getFileRangeSpecification() {
		return rangeSpecification;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.IRetrieveFileTransferContainerAdapter#sendRetrieveRequest(org.eclipse.ecf.filetransfer.identity.IFileID, org.eclipse.ecf.filetransfer.IFileRangeSpecification, org.eclipse.ecf.filetransfer.IFileTransferListener, java.util.Map)
	 */
	public void sendRetrieveRequest(IFileID rFileID, IFileRangeSpecification rangeSpec, IFileTransferListener transferListener, Map ops) throws IncomingFileTransferException {
		Assert.isNotNull(rFileID, Messages.AbstractRetrieveFileTransfer_RemoteFileID_Not_Null);
		Assert.isNotNull(transferListener, Messages.AbstractRetrieveFileTransfer_TransferListener_Not_Null);
		this.job = null;
		this.remoteFileURL = null;
		this.remoteFileID = rFileID;
		this.listener = transferListener;
		this.remoteFileContents = null;
		this.localFileContents = null;
		this.closeOutputStream = true;
		this.done = false;
		this.exception = null;
		this.bytesReceived = 0;
		this.fileLength = -1;
		this.options = ops;
		this.paused = false;
		this.rangeSpecification = rangeSpec;

		try {
			this.remoteFileURL = rFileID.getURL();
		} catch (final MalformedURLException e) {
			throw new IncomingFileTransferException(NLS.bind(Messages.AbstractRetrieveFileTransfer_MalformedURLException, rFileID), e);
		}
		setupProxies();
		openStreams();
	}

	/**
	 * Setup ECF proxy.  Subclasses must override this method to do appropriate proxy setup.  This method will be called
	 * from within {@link #sendRetrieveRequest(IFileID, IFileTransferListener, Map)} and {@link #sendRetrieveRequest(IFileID, IFileRangeSpecification, IFileTransferListener, Map)},
	 * prior to the actual call to {@link #openStreams()}.
	 * @param proxy the proxy to be setup.  Will not be <code>null</code>.
	 */
	protected abstract void setupProxy(Proxy proxy);

	/**
	 * Select a single proxy from a set of proxies available for the given host.  This implementation
	 * selects in the following manner:  1) If proxies provided is null or array of 0 length, null 
	 * is returned.  If only one proxy is available (array of length 1) then the entry is returned.
	 * If proxies provided is length > 1, then if the type of a proxy in the array matches the given
	 * protocol (e.g. http, https), then the first matching proxy is returned.  If the protocol does
	 * not match any of the proxies, then the *first* proxy (i.e. proxies[0]) is returned.  Subclasses may
	 * override if desired.
	 * 
	 * @param protocol the target protocol (e.g. http, https, scp, etc).  Will not be <code>null</code>.
	 * @param proxies the proxies to select from.  May be <code>null</code> or array of length 0.
	 * @return proxy data selected from the proxies provided.  
	 */
	protected IProxyData selectProxyFromProxies(String protocol, IProxyData[] proxies) {
		if (proxies == null || proxies.length == 0)
			return null;
		// If only one proxy is available, then use that
		if (proxies.length == 1)
			return proxies[0];
		// If more than one proxy is available, then if http/https protocol then look for that
		// one...if not found then use first
		if (protocol.equalsIgnoreCase("http")) { //$NON-NLS-1$
			for (int i = 0; i < proxies.length; i++) {
				if (proxies[i].getType().equals(IProxyData.HTTP_PROXY_TYPE))
					return proxies[i];
			}
		} else if (protocol.equalsIgnoreCase("https")) { //$NON-NLS-1$
			for (int i = 0; i < proxies.length; i++) {
				if (proxies[i].getType().equals(IProxyData.HTTPS_PROXY_TYPE))
					return proxies[i];
			}
		}
		// If we haven't found it yet, then return the first one.
		return proxies[0];
	}

	protected void setupProxies() {
		// If it's been set directly (via ECF API) then this overrides platform settings
		if (proxy == null) {
			try {
				IProxyService proxyService = Activator.getDefault().getProxyService();
				// Only do this if platform service exists
				if (proxyService != null && proxyService.isProxiesEnabled()) {
					// Setup via proxyService entry
					URL target = getRemoteFileURL();
					final IProxyData[] proxies = proxyService.getProxyDataForHost(target.getHost());
					IProxyData selectedProxy = selectProxyFromProxies(target.getProtocol(), proxies);
					if (selectedProxy != null) {
						proxy = new Proxy(((selectedProxy.getType().equalsIgnoreCase(IProxyData.SOCKS_PROXY_TYPE)) ? Proxy.Type.SOCKS : Proxy.Type.HTTP), new ProxyAddress(selectedProxy.getHost(), selectedProxy.getPort()), selectedProxy.getUserId(), selectedProxy.getPassword());
					}
				}
			} catch (Exception e) {
				// If we don't even have the classes for this (i.e. the org.eclipse.core.net plugin not available)
				// then we simply log and ignore
				Activator.logNoProxyWarning(e);
			} catch (NoClassDefFoundError e) {
				Activator.logNoProxyWarning(e);
			}
		}
		if (proxy != null)
			setupProxy(proxy);
	}

}
