/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.internal.provider.filetransfer.retrieve;

import java.net.URL;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.Proxy;
import org.eclipse.ecf.filetransfer.IFileTransferListener;
import org.eclipse.ecf.filetransfer.IRetrieveFileTransferContainerAdapter;
import org.eclipse.ecf.filetransfer.IncomingFileTransferException;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.internal.provider.filetransfer.identity.FileTransferID;
import org.eclipse.ecf.internal.provider.filetransfer.identity.FileTransferNamespace;

/**
 * Multi protocol handler for retrieve file transfer. Multiplexes between Apache
 * httpclient 3.0.1-based file retriever and the URLConnection-based file
 * retriever.
 */
public class MultiProtocolRetrieveAdapter implements
		IRetrieveFileTransferContainerAdapter {

	HttpClientRetrieveFileTransfer httpClient = new HttpClientRetrieveFileTransfer(
			new HttpClient(new MultiThreadedHttpConnectionManager()));

	UrlConnectionRetrieveFileTransfer urlClient = new UrlConnectionRetrieveFileTransfer();
	
	IConnectContext connectContext = null;
	Proxy proxy = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.filetransfer.IRetrieveFileTransferContainerAdapter#getRetrieveNamespace()
	 */
	public Namespace getRetrieveNamespace() {
		return IDFactory.getDefault().getNamespaceByName(
				FileTransferNamespace.PROTOCOL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.filetransfer.IRetrieveFileTransferContainerAdapter#setConnectContextForAuthentication(org.eclipse.ecf.core.security.IConnectContext)
	 */
	public void setConnectContextForAuthentication(
			IConnectContext connectContext) {
		this.connectContext = connectContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.filetransfer.IRetrieveFileTransferContainerAdapter#setProxy(org.eclipse.ecf.core.util.Proxy)
	 */
	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.filetransfer.IRetrieveFileTransferContainerAdapter#sendRetrieveRequest(org.eclipse.ecf.filetransfer.identity.IFileID,
	 *      org.eclipse.ecf.filetransfer.IFileTransferListener, java.util.Map)
	 */
	public void sendRetrieveRequest(IFileID remoteFileID,
			IFileTransferListener transferListener, Map options)
			throws IncomingFileTransferException {

		if (remoteFileID instanceof FileTransferID) {
			URL url = ((FileTransferID) remoteFileID).getURL();
			IRetrieveFileTransferContainerAdapter fileTransfer = null;
			if (httpClient.supportsProtocol(url.getProtocol()))
				fileTransfer = httpClient;
			else
				fileTransfer =  urlClient;

			// Set connect context
			fileTransfer.setConnectContextForAuthentication(connectContext);
			// Set Proxy
			fileTransfer.setProxy(proxy);

			// send request using given file transfer protocol

			fileTransfer.sendRetrieveRequest(remoteFileID, transferListener,
					options);

		} else
			throw new IncomingFileTransferException("invalid remoteFileID");
	}

}
