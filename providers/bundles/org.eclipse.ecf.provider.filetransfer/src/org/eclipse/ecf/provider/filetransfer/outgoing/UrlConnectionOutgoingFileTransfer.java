/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.filetransfer.outgoing;

import java.io.*;
import java.net.URLConnection;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.Proxy;
import org.eclipse.ecf.filetransfer.IIncomingFileTransferRequestListener;
import org.eclipse.ecf.filetransfer.OutgoingFileTransferException;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.filetransfer.service.ISendFileTransfer;
import org.eclipse.ecf.internal.provider.filetransfer.Messages;
import org.eclipse.ecf.provider.filetransfer.util.JREProxyHelper;
import org.eclipse.osgi.util.NLS;

public class UrlConnectionOutgoingFileTransfer extends AbstractOutgoingFileTransfer implements ISendFileTransfer {

	private static final int OK_RESPONSE_CODE = 200;

	protected URLConnection urlConnection;

	// XXX currently unused
	protected IConnectContext connectContext;

	protected long lastModifiedTime = 0L;

	protected int httpVersion = 1;

	protected int responseCode = -1;

	protected String responseMessage = null;

	protected IFileID fileid = null;

	private JREProxyHelper proxyHelper = null;

	public UrlConnectionOutgoingFileTransfer() {
		super();
		proxyHelper = new JREProxyHelper();
	}

	protected void connect() throws IOException {
		urlConnection = getRemoteFileURL().openConnection();
		urlConnection.setDoOutput(true);
		urlConnection.setUseCaches(false);
		urlConnection.setDoInput(true);
	}

	protected boolean isConnected() {
		return (urlConnection != null);
	}

	public int getResponseCode() {
		if (responseCode != -1)
			return responseCode;
		if (isHTTP()) {
			final String response = urlConnection.getHeaderField(0);
			if (response == null) {
				responseCode = -1;
				httpVersion = 1;
				return responseCode;
			}
			if (response == null || !response.startsWith("HTTP/")) //$NON-NLS-1$
				return -1;
			response.trim();
			final int mark = response.indexOf(" ") + 1; //$NON-NLS-1$
			if (mark == 0)
				return -1;
			if (response.charAt(mark - 2) != '1')
				httpVersion = 0;
			int last = mark + 3;
			if (last > response.length())
				last = response.length();
			responseCode = Integer.parseInt(response.substring(mark, last));
			if (last + 1 <= response.length())
				responseMessage = response.substring(last + 1);
		} else {
			responseCode = OK_RESPONSE_CODE;
			responseMessage = "OK"; //$NON-NLS-1$
		}

		return responseCode;

	}

	private boolean isHTTP() {
		final String protocol = getRemoteFileURL().getProtocol();
		if (protocol.equalsIgnoreCase("http") || protocol.equalsIgnoreCase("https")) //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		return false;
	}

	/**
	 * @param proxy2 the ECF proxy to setup
	 */
	protected void setupProxy(final Proxy proxy2) {
		proxyHelper.setupProxy(proxy2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.provider.filetransfer.retrieve.AbstractRetrieveFileTransfer#openStreams()
	 */
	protected void openStreams() throws OutgoingFileTransferException {
		try {
			File localFile = getFileTransferInfo().getFile();
			// Set input stream from local file
			setInputStream(new BufferedInputStream(new FileInputStream(localFile)));
			// Then connect
			connect();
			// Make PUT request
			setOutputStream(urlConnection.getOutputStream());
			fireSendStartEvent();
		} catch (final Exception e) {
			throw new OutgoingFileTransferException(NLS.bind(Messages.UrlConnectionOutgoingFileTransfer_EXCEPTION_COULD_NOT_CONNECT, getRemoteFileURL().toString()), e);
		}
	}

	private void getSendResult() {
		/*
		 XXX Under construction
		try {
			DataInputStream input = new DataInputStream(urlConnection.getInputStream());
			String string;
			while (null != ((string = input.readLine()))) {
				System.out.println(string);
			}
			input.close();
		} catch (IOException e) {

		}
		*/
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.provider.filetransfer.retrieve.AbstractRetrieveFileTransfer#hardClose()
	 */
	protected void hardClose() {
		super.hardClose();
		getSendResult();
		urlConnection = null;
		responseCode = -1;
		if (proxyHelper != null) {
			proxyHelper.dispose();
			proxyHelper = null;
		}
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

	public ID getID() {
		return fileid;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.IOutgoingFileTransferContainerAdapter#addListener(org.eclipse.ecf.filetransfer.IIncomingFileTransferRequestListener)
	 */
	public void addListener(IIncomingFileTransferRequestListener l) {
		// No listeners for incoming url connection requests.
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.IOutgoingFileTransferContainerAdapter#removeListener(org.eclipse.ecf.filetransfer.IIncomingFileTransferRequestListener)
	 */
	public boolean removeListener(IIncomingFileTransferRequestListener l) {
		// No listeners for incoming url connection requests.
		return false;
	}

}
