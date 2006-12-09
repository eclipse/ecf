/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.provider.filetransfer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.ecf.core.security.Callback;
import org.eclipse.ecf.core.security.CallbackHandler;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.security.NameCallback;
import org.eclipse.ecf.core.security.ObjectCallback;
import org.eclipse.ecf.core.security.UnsupportedCallbackException;
import org.eclipse.ecf.filetransfer.IIncomingFileTransfer;
import org.eclipse.ecf.filetransfer.IncomingFileTransferException;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveStartEvent;
import org.eclipse.ecf.filetransfer.identity.IFileID;

public class HttpClientRetrieveFileTransfer extends
		AbstractRetrieveFileTransfer {

	protected static final int DEFAULT_CONNECTION_TIMEOUT = 30000;

	protected static final String REALM_SCOPE = "realm";

	GetMethod getMethod = null;

	HttpClient client = null;

	IConnectContext connectContext = null;

	public HttpClientRetrieveFileTransfer() {
		
	}
    public HttpClientRetrieveFileTransfer(IConnectContext connectContext) {
		this.connectContext = connectContext;
	}

	protected void hardClose() {
		super.hardClose();
		if (getMethod != null) {
			getMethod.releaseConnection();
			getMethod = null;
		}
	}

	protected Credentials getCredentials() throws UnsupportedCallbackException,
			IOException {
		if (connectContext == null)
			return null;
		CallbackHandler callbackHandler = connectContext.getCallbackHandler();
		if (callbackHandler == null)
			return null;
		NameCallback usernameCallback = new NameCallback("Username:");
		ObjectCallback passwordCallback = new ObjectCallback();
		callbackHandler.handle(new Callback[] { usernameCallback,
				passwordCallback });
		return new UsernamePasswordCredentials(usernameCallback.getName(),
				(String) passwordCallback.getObject());
	}

	protected void openStreams() throws IncomingFileTransferException {
		URL theURL = null;

		try {
			theURL = getRemoteFileURL();

			client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(
					DEFAULT_CONNECTION_TIMEOUT);

			String host = theURL.getHost();
			int port = theURL.getPort();
			if (port == -1)
				port = theURL.getDefaultPort();

			Credentials credentials = getCredentials();

			if (credentials != null) {
				client.getState().setCredentials(
						new AuthScope(host, port, REALM_SCOPE), credentials);
			}

			getMethod = new GetMethod(theURL.toExternalForm());
			getMethod.setFollowRedirects(true);

			client.executeMethod(getMethod);

			long contentLength = getMethod.getResponseContentLength();
			setInputStream(getMethod.getResponseBodyAsStream());
			setFileLength(contentLength);

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
							return HttpClientRetrieveFileTransfer.this;
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
							return HttpClientRetrieveFileTransfer.this;
						}

					});
		} catch (Exception e) {
			throw new IncomingFileTransferException("Exception connecting to "
					+ theURL.toString(), e);
		}

	}

}
