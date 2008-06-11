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
package org.eclipse.ecf.internal.provider.xmpp.filetransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.filetransfer.FileTransferJob;
import org.eclipse.ecf.filetransfer.IFileTransferInfo;
import org.eclipse.ecf.filetransfer.IFileTransferListener;
import org.eclipse.ecf.filetransfer.IOutgoingFileTransfer;
import org.eclipse.ecf.filetransfer.UserCancelledException;
import org.eclipse.ecf.filetransfer.events.IFileTransferEvent;
import org.eclipse.ecf.filetransfer.events.IOutgoingFileTransferResponseEvent;
import org.eclipse.ecf.filetransfer.events.IOutgoingFileTransferSendDataEvent;
import org.eclipse.ecf.filetransfer.events.IOutgoingFileTransferSendDoneEvent;
import org.eclipse.ecf.internal.provider.xmpp.XmppPlugin;
import org.eclipse.ecf.provider.xmpp.identity.XMPPID;
import org.eclipse.osgi.util.NLS;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer.NegotiationProgress;

public class XMPPOutgoingFileTransfer implements IOutgoingFileTransfer {

	private static final int BUFFER_SIZE = 4096;

	private final ID sessionID;
	private final XMPPID remoteTarget;
	private final IFileTransferListener listener;

	private File localFile;

	private long fileSize;

	private final OutgoingFileTransfer outgoingFileTransfer;

	private long amountWritten = 0;

	private Status status;

	private Exception exception;

	private int originalOutputRequestTimeout = -1;

	private boolean localCancelled = false;

	public XMPPOutgoingFileTransfer(FileTransferManager manager, XMPPID remoteTarget, IFileTransferInfo fileTransferInfo, IFileTransferListener listener, int outgoingRequestTimeout) {
		this.remoteTarget = remoteTarget;
		this.listener = listener;
		this.sessionID = createSessionID();
		final String fullyQualifiedName = remoteTarget.getFQName();
		// Set request timeout if we have a new value 
		if (outgoingRequestTimeout != -1) {
			originalOutputRequestTimeout = OutgoingFileTransfer.getResponseTimeout();
			OutgoingFileTransfer.setResponseTimeout(outgoingRequestTimeout);
		}
		outgoingFileTransfer = manager.createOutgoingFileTransfer(fullyQualifiedName);
	}

	private ID createSessionID() {
		try {
			return IDFactory.getDefault().createGUID();
		} catch (final IDCreateException e) {
			throw new NullPointerException("cannot create id for XMPPOutgoingFileTransfer"); //$NON-NLS-1$
		}
	}

	public synchronized ID getRemoteTargetID() {
		return remoteTarget;
	}

	public ID getID() {
		return sessionID;
	}

	private void fireTransferListenerEvent(IFileTransferEvent event) {
		listener.handleTransferEvent(event);
	}

	private void setStatus(Status s) {
		this.status = s;
	}

	private void setException(Exception e) {
		this.exception = e;
	}

	private Status getStatus() {
		return this.status;
	}

	private void setErrorStatus(Exception e) {
		setStatus(FileTransfer.Status.ERROR);
		setException(e);
	}

	public synchronized void startSend(File localFile, String description) throws XMPPException {
		this.localFile = localFile;
		this.fileSize = localFile.length();
		setStatus(Status.INITIAL);
		final NegotiationProgress progress = new NegotiationProgress();

		outgoingFileTransfer.sendFile(localFile.getAbsolutePath(), this.fileSize, description, progress);

		final Thread transferThread = new Thread(new Runnable() {
			public void run() {
				setStatus(outgoingFileTransfer.getStatus());
				boolean negotiation = true;
				try {
					while (negotiation && !localCancelled) {
						// check the state of the progress
						try {
							Thread.sleep(300);
						} catch (final InterruptedException e) {
							setErrorStatus(e);
							return;
						}
						final Status s = progress.getStatus();
						setStatus(s);
						final boolean negotiated = getStatus().equals(Status.NEGOTIATED);
						if (s.equals(Status.NEGOTIATED) || s.equals(Status.CANCLED) || s.equals(Status.COMPLETE) || s.equals(Status.ERROR) || s.equals(Status.REFUSED)) {
							fireTransferListenerEvent(new IOutgoingFileTransferResponseEvent() {
								private static final long serialVersionUID = -5940612388464073240L;

								public boolean requestAccepted() {
									return negotiated;
								}

								public IOutgoingFileTransfer getSource() {
									return XMPPOutgoingFileTransfer.this;
								}

								public String toString() {
									final StringBuffer buf = new StringBuffer("OutgoingFileTransferResponseEvent["); //$NON-NLS-1$
									buf.append("requestAccepted=").append(requestAccepted()).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
									return buf.toString();
								}

								public void setFileTransferJob(FileTransferJob job) {
									// does nothing with this implementation
								}
							});
							// And negotiation is over
							negotiation = false;
						}
					}

					if (localCancelled) {
						setErrorStatus(new UserCancelledException("Transfer cancelled by sender")); //$NON-NLS-1$
						return;
					}

					final OutputStream outs = progress.getOutputStream();

					if (outs == null) {
						setErrorStatus(new IOException("No output stream available")); //$NON-NLS-1$
						return;
					}

					writeToStream(new FileInputStream(XMPPOutgoingFileTransfer.this.localFile), outs);
					setStatus(Status.COMPLETE);
				} catch (final Exception e) {
					setStatus(FileTransfer.Status.ERROR);
					setException(e);
				} finally {
					// Reset request timeout
					if (originalOutputRequestTimeout != -1) {
						OutgoingFileTransfer.setResponseTimeout(originalOutputRequestTimeout);
					}
					// Then notify that the sending is done
					fireTransferListenerEvent(new IOutgoingFileTransferSendDoneEvent() {
						private static final long serialVersionUID = -6315336868737148845L;

						public IOutgoingFileTransfer getSource() {
							return XMPPOutgoingFileTransfer.this;
						}

						public String toString() {
							final StringBuffer buf = new StringBuffer("IOutgoingFileTransferSendDoneEvent["); //$NON-NLS-1$
							buf.append("isDone=" + getSource().isDone()); //$NON-NLS-1$
							buf.append(";bytesSent=").append(getSource().getBytesSent()); //$NON-NLS-1$
							buf.append(";exception=").append(getException()).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
							return buf.toString();
						}
					});
				}
			}
		}, NLS.bind("XMPP send {0}", remoteTarget.toExternalForm())); //$NON-NLS-1$

		transferThread.start();
	}

	public synchronized void cancel() {
		localCancelled = true;
	}

	public synchronized File getLocalFile() {
		return localFile;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == null)
			return null;
		if (adapter.isInstance(this))
			return this;
		final IAdapterManager adapterManager = XmppPlugin.getDefault().getAdapterManager();
		return (adapterManager == null) ? null : adapterManager.loadAdapter(this, adapter.getName());
	}

	public long getBytesSent() {
		return amountWritten;
	}

	public Exception getException() {
		return exception;
	}

	public double getPercentComplete() {
		return (fileSize <= 0) ? 1.0 : (((double) amountWritten) / ((double) fileSize));
	}

	public boolean isDone() {
		return status == Status.CANCLED || status == Status.ERROR || status == Status.COMPLETE;
	}

	public ID getSessionID() {
		return sessionID;
	}

	protected void writeToStream(final InputStream in, final OutputStream out) throws XMPPException, IOException, UserCancelledException {
		final byte[] b = new byte[BUFFER_SIZE];
		int count = 0;
		amountWritten = 0;
		try {
			do {

				if (localCancelled)
					throw new UserCancelledException("Transfer cancelled by sender"); //$NON-NLS-1$

				out.write(b, 0, count);

				amountWritten += count;

				if (count > 0) {
					fireTransferListenerEvent(new IOutgoingFileTransferSendDataEvent() {
						private static final long serialVersionUID = 2327297070577249812L;

						public IOutgoingFileTransfer getSource() {
							return XMPPOutgoingFileTransfer.this;
						}

						public String toString() {
							final StringBuffer buf = new StringBuffer("IOutgoingFileTransferSendDataEvent["); //$NON-NLS-1$
							buf.append("bytesSent=").append(getSource().getBytesSent()); //$NON-NLS-1$
							buf.append(";percentComplete=").append(getSource().getPercentComplete()).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
							return buf.toString();
						}

					});
				}
				// read more bytes from the input stream
				count = in.read(b);
			} while (count != -1 && !getStatus().equals(Status.CANCLED));

			// the connection was likely terminated abruptly if these are not equal
			if (!getStatus().equals(Status.CANCLED) && amountWritten != fileSize) {
				setStatus(Status.ERROR);
			}
		} finally {
			out.flush();
			out.close();
			in.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.IFileTransfer#getFileLength()
	 */
	public long getFileLength() {
		return fileSize;
	}

}
