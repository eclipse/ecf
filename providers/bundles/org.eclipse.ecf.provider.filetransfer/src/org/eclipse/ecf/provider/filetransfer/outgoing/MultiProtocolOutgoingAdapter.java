/****************************************************************************
 * Copyright (c) 2004, 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.provider.filetransfer.outgoing;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.Proxy;
import org.eclipse.ecf.filetransfer.*;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.filetransfer.service.ISendFileTransfer;
import org.eclipse.ecf.internal.provider.filetransfer.Activator;
import org.eclipse.ecf.internal.provider.filetransfer.Messages;
import org.eclipse.ecf.provider.filetransfer.identity.FileTransferNamespace;

/**
 * Multi protocol handler for outgoing file transfer. Multiplexes between Apache
 * httpclient 3.0.1-based file retriever and the URLConnection-based file
 * retriever.
 */
public class MultiProtocolOutgoingAdapter implements ISendFileTransfer {

	IConnectContext connectContext = null;
	Proxy proxy = null;

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.IOutgoingFileTransferContainerAdapter#getOutgoingNamespace()
	 */
	public Namespace getOutgoingNamespace() {
		return IDFactory.getDefault().getNamespaceByName(FileTransferNamespace.PROTOCOL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.filetransfer.IRetrieveFileTransferContainerAdapter#setConnectContextForAuthentication(org.eclipse.ecf.core.security.IConnectContext)
	 */
	public void setConnectContextForAuthentication(IConnectContext connectContext) {
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

	public void sendOutgoingRequest(IFileID remoteFileID, File outgoingFile, IFileTransferListener transferListener, Map options) throws OutgoingFileTransferException {

		String protocol = null;
		try {
			protocol = remoteFileID.getURL().getProtocol();
		} catch (final MalformedURLException e) {
			throw new OutgoingFileTransferException(Messages.AbstractRetrieveFileTransfer_MalformedURLException);
		}

		IOutgoingFileTransferContainerAdapter fileTransfer = null;
		fileTransfer = Activator.getDefault().getSendFileTransfer(protocol);

		// We will default to JRE-provided file transfer if nothing else
		// available
		// for given protocol
		if (fileTransfer == null)
			fileTransfer = new UrlConnectionOutgoingFileTransfer();

		// Set connect context
		fileTransfer.setConnectContextForAuthentication(connectContext);
		// Set Proxy
		fileTransfer.setProxy(proxy);

		// send request using given file transfer protocol
		fileTransfer.sendOutgoingRequest(remoteFileID, outgoingFile, transferListener, options);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.IOutgoingFileTransferContainerAdapter#addListener(org.eclipse.ecf.filetransfer.IIncomingFileTransferRequestListener)
	 */
	public void addListener(IIncomingFileTransferRequestListener listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.IOutgoingFileTransferContainerAdapter#removeListener(org.eclipse.ecf.filetransfer.IIncomingFileTransferRequestListener)
	 */
	public boolean removeListener(IIncomingFileTransferRequestListener listener) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.IOutgoingFileTransferContainerAdapter#sendOutgoingRequest(org.eclipse.ecf.filetransfer.identity.IFileID, org.eclipse.ecf.filetransfer.IFileTransferInfo, org.eclipse.ecf.filetransfer.IFileTransferListener, java.util.Map)
	 */
	public void sendOutgoingRequest(IFileID targetReceiver, IFileTransferInfo localFileToSend, IFileTransferListener transferListener, Map options) throws OutgoingFileTransferException {
		String protocol = null;
		try {
			protocol = targetReceiver.getURL().getProtocol();
		} catch (final MalformedURLException e) {
			throw new OutgoingFileTransferException(Messages.AbstractRetrieveFileTransfer_MalformedURLException);
		}

		IOutgoingFileTransferContainerAdapter fileTransfer = null;
		fileTransfer = Activator.getDefault().getSendFileTransfer(protocol);

		// We will default to JRE-provided file transfer if nothing else
		// available
		// for given protocol
		if (fileTransfer == null)
			fileTransfer = new UrlConnectionOutgoingFileTransfer();

		// Set connect context
		fileTransfer.setConnectContextForAuthentication(connectContext);
		// Set Proxy
		fileTransfer.setProxy(proxy);

		// send request using given file transfer protocol
		fileTransfer.sendOutgoingRequest(targetReceiver, localFileToSend, transferListener, options);

	}

}
