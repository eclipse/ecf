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

package org.eclipse.ecf.internal.provider.xmpp;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.filetransfer.IFileTransferInfo;
import org.eclipse.ecf.filetransfer.IIncomingFileTransfer;
import org.eclipse.ecf.filetransfer.IIncomingFileTransferRequestListener;
import org.eclipse.ecf.filetransfer.IncomingFileTransferException;
import org.eclipse.ecf.filetransfer.events.IFileTransferRequestEvent;
import org.eclipse.ecf.internal.provider.xmpp.identity.XMPPID;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;

public class XMPPFileTransferListener implements FileTransferListener {

	IIncomingFileTransferRequestListener listener;
	
	IncomingFileTransfer incoming = null;
	
	IContainer container = null;
	
	/**
	 * @param listener
	 */
	public XMPPFileTransferListener(IContainer container,
			IIncomingFileTransferRequestListener listener) {
		this.container = container;
		this.listener = listener;
	}

	/* (non-Javadoc)
	 * @see org.jivesoftware.smackx.filetransfer.FileTransferListener#fileTransferRequest(org.jivesoftware.smackx.filetransfer.FileTransferRequest)
	 */
	public void fileTransferRequest(final FileTransferRequest request) {
		listener.handleFileTransferRequest(new IFileTransferRequestEvent() {
			
			private static final long serialVersionUID = -6173401619917403353L;

			boolean requestAccepted = false;
			
			IFileTransferInfo fileTransferInfo = new IFileTransferInfo() {

				Map props = new HashMap();
				
				File f = new File(request.getFileName());
				
				public String getDescription() {
					return request.getDescription();
				}

				public File getFile() {
					return f;
				}

				public Map getProperties() {
					return props;
				}

				public Object getAdapter(Class adapter) {
					return null;
				}

				public long getFileSize() {
					return request.getFileSize();
				}

				public String getMimeType() {
					return request.getMimeType();
				}
				
				public String toString() {
					StringBuffer buf = new StringBuffer("FileTransferInfo[");
					buf.append("file=").append(f);
					buf.append(";size=").append(getFileSize());
					buf.append(";description="+getDescription());
					buf.append(";mimeType=").append(getMimeType()).append("]");
					return buf.toString();
				}
			
			};
			public IIncomingFileTransfer accept(File localFileToSave)
					throws IncomingFileTransferException {
				incoming = request.accept();
				try {
					incoming.recieveFile(localFileToSave);
					requestAccepted = true;
					return new IIncomingFileTransfer() {

						public long getBytesReceived() {
							return incoming.getAmountWritten();
						}

						public void cancel() {
							incoming.cancel();
						}

						public Exception getException() {
							return incoming.getException();
						}

						public double getPercentComplete() {
							return incoming.getProgress();
						}

						public boolean isDone() {
							return incoming.isDone();
						}

						public Object getAdapter(Class adapter) {
							return null;
						}

						public ID getID() {
							return createThreadID(request.getStreamID());
						}
						
						public String toString() {
							StringBuffer buf = new StringBuffer("IIncomingFileTransfer[");
							buf.append("id=").append(getID());
							buf.append(";bytesWritten="+getBytesReceived());
							buf.append(";percentComplete=").append(getPercentComplete());
							buf.append(";isDone=").append(isDone());
							buf.append(";exception=").append(getException()).append("]");
							return buf.toString();
						}
					};
				} catch (XMPPException e) {
					throw new IncomingFileTransferException("handleFileTransferRequest",e);
				}
			}

			public IFileTransferInfo getFileTransferInfo() {
				return fileTransferInfo;
			}

			public ID getRequesterID() {
				return createIDFromName(request.getRequestor());
			}

			public void reject() {
				request.reject();
			}

			public boolean requestAccepted() {
				return requestAccepted;
			}
			
			public String toString() {
				StringBuffer buf = new StringBuffer("FileTransferRequestEvent[");
				buf.append("requester=").append(getRequesterID());
				buf.append(";requestAccepted=").append(requestAccepted());
				buf.append(";transferInfo=").append(getFileTransferInfo()).append("]");
				return buf.toString();
			}
		});
	}

	private XMPPID createIDFromName(String uname) {
		try {
			return new XMPPID(container.getConnectNamespace(), uname);
		} catch (Exception e) {
			return null;
		}
	}

	private ID createThreadID(String thread) {
		try {
			if (thread == null || thread.equals(""))
				return null;
			return IDFactory.getDefault().createStringID(thread);
		} catch (Exception e) {
			return null;
		}

	}

}
