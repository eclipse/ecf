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
package org.eclipse.ecf.provider.xmpp.filetransfer;

import java.io.File;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.IDInstantiationException;
import org.eclipse.ecf.filetransfer.IFileTransferInfo;
import org.eclipse.ecf.filetransfer.IFileTransferListener;
import org.eclipse.ecf.filetransfer.IOutgoingFileTransfer;
import org.eclipse.ecf.provider.xmpp.identity.XMPPID;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

public class XMPPOutgoingFileTransfer implements IOutgoingFileTransfer {

	ID sessionID;
	XMPPID remoteTarget;
	IFileTransferInfo transferInfo;
	IFileTransferListener listener;
	FileTransferManager manager;
	
	File localFile;
	
	OutgoingFileTransfer outgoingFileTransfer;
	
	public XMPPOutgoingFileTransfer(FileTransferManager manager, XMPPID remoteTarget, IFileTransferInfo fileTransferInfo, IFileTransferListener listener) {
		this.manager = manager;
		this.remoteTarget = remoteTarget;
		this.transferInfo = fileTransferInfo;
		this.listener = listener;
		this.sessionID = createSessionID();
		outgoingFileTransfer = manager.createOutgoingFileTransfer(remoteTarget.getName()+XMPPID.PATH_DELIMITER+remoteTarget.getResourceName());		
	}
	protected ID createSessionID() {
		try {
			return IDFactory.getDefault().createGUID();
		} catch (IDInstantiationException e) {
			throw new NullPointerException("cannot create id for XMPPOutgoingFileTransfer");
		}
	}
	public synchronized ID getRemoteTargetID() {
		return remoteTarget;
	}
	public ID getID() {
		return sessionID;
	}
	public synchronized void startSend(File localFile, String description) throws XMPPException {
		outgoingFileTransfer.sendFile(localFile, description);
		this.localFile = localFile;
	}

	public synchronized void cancel() {
		if (outgoingFileTransfer != null) {
			outgoingFileTransfer.cancel();
			outgoingFileTransfer = null;
		}
	}

	public synchronized File getLocalFile() {
		return localFile;
	}

	public Object getAdapter(Class adapter) {
		return null;
	}
	public long getBytesSent() {
		return outgoingFileTransfer.getAmountWritten();
	}
	public Exception getException() {
		return outgoingFileTransfer.getException();
	}
	public double getPercentComplete() {
		return outgoingFileTransfer.getProgress();
	}
	public boolean isDone() {
		return outgoingFileTransfer.isDone();
	}
	public ID getSessionID() {
		return sessionID;
	}

}
