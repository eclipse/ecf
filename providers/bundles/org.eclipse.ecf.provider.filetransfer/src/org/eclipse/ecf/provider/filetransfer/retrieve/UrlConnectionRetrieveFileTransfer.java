/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.filetransfer.retrieve;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.Proxy;
import org.eclipse.ecf.filetransfer.IFileTransferPausable;
import org.eclipse.ecf.filetransfer.IncomingFileTransferException;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.provider.filetransfer.identity.FileTransferID;

public class UrlConnectionRetrieveFileTransfer extends AbstractRetrieveFileTransfer {

	private static final int HTTP_RANGE_RESPONSE = 206;

	protected URLConnection urlConnection;

	// XXX currently unused
	protected IConnectContext connectContext;
	// XXX currently unused
	protected Proxy proxy;

	protected IFileID fileid = null;

	protected long lastModifiedTime = 0L;

	protected int httpVersion = 1;

	protected int responseCode = -1;

	protected String responseMessage = null;

	protected void connect(URL theURL) throws IOException {
		urlConnection = theURL.openConnection();
	}

	protected boolean isConnected() {
		return (urlConnection != null);
	}

	protected void setResumeRequestHeaderValues() throws IOException {
		if (this.bytesReceived <= 0 || this.fileLength <= this.bytesReceived)
			throw new IOException("resume start error");
		urlConnection.setRequestProperty("Range:", "bytes=" + this.bytesReceived + "-");
	}

	public int getResponseCode() {
		if (responseCode != -1)
			return responseCode;
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
		return responseCode;
	}

	private boolean isHTTP11() {
		return (httpVersion >= 1);
	}

	protected void getResponseHeaderValues(URL theURL) throws IOException {
		if (!isConnected())
			throw new ConnectException("not connected");
		if (getResponseCode() == -1)
			throw new IOException("invalid server response");
		lastModifiedTime = urlConnection.getLastModified();
		setFileLength(urlConnection.getContentLength());
		fileid = new FileTransferID(getRetrieveNamespace(), theURL);
	}

	protected void getResumeResponseHeaderValues(URL theURL) throws IOException {
		if (!isConnected())
			throw new ConnectException("not connected");
		if (getResponseCode() != HTTP_RANGE_RESPONSE)
			throw new IOException("invalid server response to partial range request");
		if (lastModifiedTime != urlConnection.getLastModified())
			throw new IOException("file modified since last access");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.filetransfer.retrieve.AbstractRetrieveFileTransfer#openStreams()
	 */
	protected void openStreams() throws IncomingFileTransferException {
		final URL theURL = getRemoteFileURL();
		try {
			connect(theURL);
			// Make actual GET request
			setInputStream(urlConnection.getInputStream());
			getResponseHeaderValues(theURL);
			fireReceiveStartEvent();
		} catch (final Exception e) {
			throw new IncomingFileTransferException("Exception connecting to " //$NON-NLS-1$
					+ theURL.toString(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.provider.filetransfer.retrieve.AbstractRetrieveFileTransfer#hardClose()
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.IIdentifiable#getID()
	 */
	public ID getID() {
		return fileid;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.filetransfer.retrieve.AbstractRetrieveFileTransfer#doPause()
	 */
	protected boolean doPause() {
		if (isPaused() || !isConnected() || isDone())
			return false;
		hardClose();
		this.paused = true;
		return this.paused;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.filetransfer.retrieve.AbstractRetrieveFileTransfer#doResume()
	 */
	protected boolean doResume() {
		if (!isPaused() || isConnected() || isDone())
			return false;
		return openStreamsForResume();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.filetransfer.retrieve.AbstractRetrieveFileTransfer#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter != null)
			return null;
		if (adapter.equals(IFileTransferPausable.class)) {
			final String protocol = getRemoteFileURL().getProtocol();
			if ((protocol.equals("http") || protocol.equals("https")) && isHTTP11())
				return this;
			else
				return null;
		} else
			return super.getAdapter(adapter);
	}

	/**
	 * @return
	 */
	private boolean openStreamsForResume() {
		final URL theURL = getRemoteFileURL();
		try {
			connect(theURL);
			setResumeRequestHeaderValues();
			// Make actual GET request
			setInputStream(urlConnection.getInputStream());
			getResumeResponseHeaderValues(theURL);
			this.paused = false;
			fireReceiveResumedEvent();
			return true;
		} catch (final Exception e) {
			this.exception = e;
			this.done = true;
			this.paused = false;
			hardClose();
			fireTransferReceiveDoneEvent();
			return false;
		}
	}
}
