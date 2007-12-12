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
import org.eclipse.osgi.util.NLS;

/**
 * Multi protocol handler for outgoing file transfer. Multiplexes between Apache
 * httpclient 3.0.1-based file retriever and the URLConnection-based file
 * retriever.
 */
public class MultiProtocolOutgoingAdapter implements ISendFileTransfer {

	IConnectContext connectContext = null;
	Proxy proxy = null;

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.ISendFileTransferContainerAdapter#getOutgoingNamespace()
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

	public void sendOutgoingRequest(IFileID targetID, File outgoingFile, IFileTransferListener transferListener, Map options) throws SendFileTransferException {
		String protocol = null;
		try {
			protocol = targetID.getURL().getProtocol();
		} catch (final MalformedURLException e) {
			throw new SendFileTransferException(Messages.AbstractRetrieveFileTransfer_MalformedURLException);
		}

		ISendFileTransferContainerAdapter fileTransfer = null;
		fileTransfer = Activator.getDefault().getSendFileTransfer(protocol);

		// If no handler setup for this protocol then throw
		if (fileTransfer == null) {
			throw new SendFileTransferException(NLS.bind(Messages.MultiProtocolOutgoingAdapter_EXCEPTION_NO_PROTOCOL_HANDER, targetID));
		}

		fileTransfer.setConnectContextForAuthentication(connectContext);
		fileTransfer.setProxy(proxy);
		fileTransfer.sendOutgoingRequest(targetID, outgoingFile, transferListener, options);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.ISendFileTransferContainerAdapter#addListener(org.eclipse.ecf.filetransfer.IIncomingFileTransferRequestListener)
	 */
	public void addListener(IIncomingFileTransferRequestListener listener) {
		// We don't have any listeners
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.ISendFileTransferContainerAdapter#removeListener(org.eclipse.ecf.filetransfer.IIncomingFileTransferRequestListener)
	 */
	public boolean removeListener(IIncomingFileTransferRequestListener listener) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.ISendFileTransferContainerAdapter#sendOutgoingRequest(org.eclipse.ecf.filetransfer.identity.IFileID, org.eclipse.ecf.filetransfer.IFileTransferInfo, org.eclipse.ecf.filetransfer.IFileTransferListener, java.util.Map)
	 */
	public void sendOutgoingRequest(IFileID targetID, IFileTransferInfo localFileToSend, IFileTransferListener transferListener, Map options) throws SendFileTransferException {
		String protocol = null;
		try {
			protocol = targetID.getURL().getProtocol();
		} catch (final MalformedURLException e) {
			throw new SendFileTransferException(Messages.AbstractRetrieveFileTransfer_MalformedURLException);
		}

		ISendFileTransferContainerAdapter fileTransfer = null;
		fileTransfer = Activator.getDefault().getSendFileTransfer(protocol);

		// If no handler setup for this protocol then throw
		if (fileTransfer == null) {
			throw new SendFileTransferException(NLS.bind(Messages.MultiProtocolOutgoingAdapter_EXCEPTION_NO_PROTOCOL_HANDER, targetID));
		}

		fileTransfer.setConnectContextForAuthentication(connectContext);
		fileTransfer.setProxy(proxy);
		fileTransfer.sendOutgoingRequest(targetID, localFileToSend, transferListener, options);
	}

}
