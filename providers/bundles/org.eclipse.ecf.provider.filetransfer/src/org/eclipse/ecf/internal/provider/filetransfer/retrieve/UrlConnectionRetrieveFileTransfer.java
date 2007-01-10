/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.provider.filetransfer.retrieve;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.Proxy;
import org.eclipse.ecf.filetransfer.IIncomingFileTransfer;
import org.eclipse.ecf.filetransfer.IncomingFileTransferException;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveStartEvent;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.internal.provider.filetransfer.identity.FileTransferID;

public class UrlConnectionRetrieveFileTransfer extends AbstractRetrieveFileTransfer {

	URLConnection urlConnection;
	
    // XXX currently unused
	IConnectContext connectContext;
	// XXX currently unused
	Proxy proxy;
	
	protected void openStreams(Map options) throws IncomingFileTransferException {
		URL theURL = null;

		try {

			theURL = getRemoteFileURL();
			urlConnection = theURL.openConnection();
			setInputStream(urlConnection.getInputStream());
			setFileLength(urlConnection.getContentLength());

			listener
					.handleTransferEvent(new IIncomingFileTransferReceiveStartEvent() {
						private static final long serialVersionUID = -59096575294481755L;

						public IFileID getFileID() {
							return remoteFileID;
						}

						public IIncomingFileTransfer receive(
								File localFileToSave) throws IOException {
							setOutputStream(new BufferedOutputStream(
									new FileOutputStream(localFileToSave)));
							job = new FileTransferJob(getRemoteFileURL()
									.toString());
							job.schedule();
							return UrlConnectionRetrieveFileTransfer.this;
						}

						public String toString() {
							StringBuffer sb = new StringBuffer(
									"IIncomingFileTransferReceiveStartEvent[");
							sb.append("isdone=").append(done).append(";");
							sb.append("bytesReceived=").append(bytesReceived)
									.append("]");
							return sb.toString();
						}

						public void cancel() {
							hardClose();
						}

						public IIncomingFileTransfer receive(
								OutputStream streamToStore) throws IOException {
							setOutputStream(new BufferedOutputStream(
									streamToStore));
							setCloseOutputStream(false);
							job = new FileTransferJob(getRemoteFileURL()
									.toString());
							job.schedule();
							return UrlConnectionRetrieveFileTransfer.this;
						}

					});
		} catch (Exception e) {
			throw new IncomingFileTransferException("Exception connecting to "
					+ theURL.toString(), e);
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.internal.provider.filetransfer.retrieve.AbstractRetrieveFileTransfer#hardClose()
	 */
	protected void hardClose() {
		super.hardClose();
		urlConnection = null;
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

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.IIdentifiable#getID()
	 */
	public ID getID() {
		return new FileTransferID(getRetrieveNamespace(),getRemoteFileURL());
	}


}
