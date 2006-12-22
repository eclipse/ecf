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

package org.eclipse.ecf.provider.xmpp;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.filetransfer.BaseFileTransferInfo;
import org.eclipse.ecf.filetransfer.IFileTransferInfo;
import org.eclipse.ecf.filetransfer.IFileTransferListener;
import org.eclipse.ecf.filetransfer.IIncomingFileTransferRequestListener;
import org.eclipse.ecf.filetransfer.IOutgoingFileTransferContainerAdapter;
import org.eclipse.ecf.filetransfer.OutgoingFileTransferException;
import org.eclipse.ecf.filetransfer.events.IFileTransferEvent;
import org.eclipse.ecf.provider.xmpp.filetransfer.XMPPOutgoingFileTransfer;
import org.eclipse.ecf.provider.xmpp.identity.XMPPID;
import org.eclipse.ecf.provider.xmpp.smack.ECFConnection;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;

public class XMPPOutgoingFileTransferContainerAdapter implements
		IOutgoingFileTransferContainerAdapter {

	List transferListeners = new ArrayList();

	List incomingTransferListeners = new ArrayList();

	XMPPContainer container = null;

	public XMPPOutgoingFileTransferContainerAdapter(XMPPContainer container) {
		this.container = container;
	}

	public void dispose() {
		if (transferListeners != null)
			transferListeners.clear();
		transferListeners = null;

		if (incomingTransferListeners != null)
			incomingTransferListeners.clear();
		incomingTransferListeners = null;
		container = null;
	}

	protected void addFileTransferListener(IFileTransferListener listener) {
		transferListeners.add(listener);
	}

	protected void removeFileTransferListener(IFileTransferListener listener) {
		transferListeners.remove(listener);
	}

	public void addListener(IIncomingFileTransferRequestListener listener) {
		incomingTransferListeners.add(listener);
	}

	public void sendOutgoingRequest(ID targetReceiver,
			IFileTransferInfo localFileToSend,
			IFileTransferListener progressListener, Map options)
			throws OutgoingFileTransferException {
		ECFConnection ecfConnection = container.getECFConnection();
		if (ecfConnection == null || !ecfConnection.isConnected())
			throw new OutgoingFileTransferException("not connected");
		XMPPConnection xmppConnection = ecfConnection.getXMPPConnection();
		FileTransferManager manager = new FileTransferManager(xmppConnection);

		XMPPOutgoingFileTransfer fileTransfer = new XMPPOutgoingFileTransfer(
				manager, (XMPPID) targetReceiver, localFileToSend,
				progressListener);

		try {
			fileTransfer.startSend(localFileToSend.getFile(), localFileToSend
					.getDescription());
		} catch (XMPPException e) {
			throw new OutgoingFileTransferException(
					"Exception sending start request", e);
		}
	}

	protected void fireFileTransferEvent(IFileTransferEvent event) {
		for (Iterator i = transferListeners.iterator(); i.hasNext();) {
			IFileTransferListener l = (IFileTransferListener) i.next();
			l.handleTransferEvent(event);
		}
	}

	public Namespace getOutgoingFileTransferNamespace() {
		return container.getConnectNamespace();
	}

	public boolean removeListener(IIncomingFileTransferRequestListener listener) {
		return incomingTransferListeners.remove(listener);
	}

	public void sendOutgoingRequest(ID targetReceiver, File localFileToSend,
			IFileTransferListener transferListener, Map options)
			throws OutgoingFileTransferException {
		sendOutgoingRequest(targetReceiver, new BaseFileTransferInfo(
				localFileToSend), transferListener, options);
	}

}
