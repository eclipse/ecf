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

import java.net.URL;
import java.util.Arrays;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.Proxy;
import org.eclipse.ecf.core.util.ProxyAddress;
import org.eclipse.ecf.filetransfer.*;
import org.eclipse.ecf.filetransfer.events.IRemoteFileSystemBrowseEvent;
import org.eclipse.ecf.filetransfer.events.IRemoteFileSystemEvent;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.internal.provider.filetransfer.Activator;
import org.eclipse.ecf.internal.provider.filetransfer.Messages;

/**
 * Abstract class for browsing an efs file system.
 */
public abstract class AbstractFileSystemBrowser {

	protected IFileID fileID = null;
	protected IRemoteFileSystemListener listener = null;

	protected Exception exception = null;
	protected IRemoteFile[] remoteFiles = null;

	protected Proxy proxy;
	protected URL directoryOrFile;

	protected IConnectContext connectContext;

	Job job = null;
	Object lock = new Object();

	class DirectoryJob extends Job {

		public DirectoryJob() {
			super(fileID.getName());
		}

		protected IStatus run(IProgressMonitor monitor) {
			try {
				if (monitor.isCanceled())
					throw new UserCancelledException(Messages.AbstractRetrieveFileTransfer_Exception_User_Cancelled);
				runRequest();
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
	 * Run the actual request.  This method is called within the job created to actually get the
	 * directory or file information.
	 * @throws Exception if some problem with making the request or receiving response to the request.
	 */
	protected abstract void runRequest() throws Exception;

	public AbstractFileSystemBrowser(IFileID directoryOrFileID, IRemoteFileSystemListener listener, URL url, IConnectContext connectContext, Proxy proxy) {
		Assert.isNotNull(directoryOrFileID);
		this.fileID = directoryOrFileID;
		Assert.isNotNull(listener);
		this.listener = listener;
		this.directoryOrFile = url;
		this.connectContext = connectContext;
		this.proxy = proxy;
	}

	public IRemoteFileSystemRequest sendBrowseRequest() {
		job = new DirectoryJob();
		job.schedule();
		return new IRemoteFileSystemRequest() {

			public void cancel() {
				synchronized (lock) {
					if (job != null)
						job.cancel();
				}
			}

			public IFileID getFileID() {
				return fileID;
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
		return new IRemoteFileSystemBrowseEvent() {

			public IFileID getFileID() {
				return fileID;
			}

			public Exception getException() {
				return exception;
			}

			public String toString() {
				StringBuffer buf = new StringBuffer("RemoteFileSystemBrowseEvent["); //$NON-NLS-1$
				buf.append("fileID=").append(fileID).append(";"); //$NON-NLS-1$ //$NON-NLS-2$
				buf.append("files=" + Arrays.asList(remoteFiles)).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
				return buf.toString();
			}

			public IRemoteFile[] getRemoteFiles() {
				return remoteFiles;
			}
		};
	}

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
				if (proxyService != null) {
					// Setup via proxyService entry
					URL target = directoryOrFile;
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
