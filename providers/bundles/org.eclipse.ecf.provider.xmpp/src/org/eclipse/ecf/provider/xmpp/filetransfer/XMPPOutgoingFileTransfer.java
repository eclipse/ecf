package org.eclipse.ecf.provider.xmpp.filetransfer;

import java.io.File;
import java.util.Map;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.IDInstantiationException;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.filetransfer.IFileTransferListener;
import org.eclipse.ecf.filetransfer.IOutgoingFileTransfer;
import org.eclipse.ecf.provider.xmpp.identity.XMPPID;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

public class XMPPOutgoingFileTransfer implements IOutgoingFileTransfer {

	ID sessionID;
	XMPPID remoteTarget;
	IFileTransferListener listener;
	FileTransferManager manager;
	
	File localFile;
	
	OutgoingFileTransfer outgoingFileTransfer;
	
	public XMPPOutgoingFileTransfer(XMPPID remoteTarget, IFileTransferListener listener, FileTransferManager manager) {
		this.remoteTarget = remoteTarget;
		this.listener = listener;
		this.manager = manager;
		this.sessionID = createSessionID();
		
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
	public IFileTransferListener getListener() {
		return listener;
	}
	public synchronized void startSend(File localFile, Map options) throws ECFException {
		if (localFile == null) throw new NullPointerException("localFile cannot be null");
		if (!localFile.exists()) throw new ECFException("localFile "+localFile+" does not exist");
		outgoingFileTransfer = manager.createOutgoingFileTransfer(remoteTarget.getName()+XMPPID.PATH_DELIMITER+remoteTarget.getResourceName());
		try {
			outgoingFileTransfer.sendFile(localFile, "from ECF");
			this.localFile = localFile;
		} catch (XMPPException e) {
			throw new ECFException("Exception sending file "+localFile);
		}
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

}
