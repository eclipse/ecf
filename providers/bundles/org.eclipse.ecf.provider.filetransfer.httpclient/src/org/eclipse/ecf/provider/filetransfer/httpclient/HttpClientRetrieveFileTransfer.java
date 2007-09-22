/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.filetransfer.httpclient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import javax.security.auth.login.LoginException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.security.Callback;
import org.eclipse.ecf.core.security.CallbackHandler;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.security.NameCallback;
import org.eclipse.ecf.core.security.ObjectCallback;
import org.eclipse.ecf.core.security.UnsupportedCallbackException;
import org.eclipse.ecf.core.util.Proxy;
import org.eclipse.ecf.core.util.ProxyAddress;
import org.eclipse.ecf.filetransfer.IIncomingFileTransfer;
import org.eclipse.ecf.filetransfer.IncomingFileTransferException;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveStartEvent;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.internal.provider.filetransfer.httpclient.Messages;
import org.eclipse.ecf.provider.filetransfer.identity.FileTransferID;
import org.eclipse.ecf.provider.filetransfer.retrieve.AbstractRetrieveFileTransfer;

public class HttpClientRetrieveFileTransfer extends AbstractRetrieveFileTransfer {

	private static final String HTTP_PROXY_PORT = Messages.HttpClientRetrieveFileTransfer_Http_ProxyPort_Prop;

	private static final String HTTP_PROXY_HOST = Messages.HttpClientRetrieveFileTransfer_Http_ProxyHost_Prop;

	private static final String USERNAME_PREFIX = Messages.HttpClientRetrieveFileTransfer_Username_Prefix;

	protected static final int DEFAULT_CONNECTION_TIMEOUT = 30000;

	protected static final int HTTP_PORT = 80;

	protected static final int HTTPS_PORT = 443;

	protected static final int MAX_RETRY = 2;

	protected static final String HTTPS = Messages.FileTransferNamespace_Https_Protocol;

	protected static final String HTTP = Messages.FileTransferNamespace_Http_Protocol;

	protected static final String[] supportedProtocols = {HTTP, HTTPS};

	private GetMethod getMethod = null;

	private HttpClient httpClient = null;

	private IConnectContext fileRequestConnectContext = null;

	private String username;

	private String password;

	private Proxy proxy;

	public HttpClientRetrieveFileTransfer(HttpClient httpClient) {
		if (httpClient == null)
			throw new NullPointerException("httpClient cannot be null"); //$NON-NLS-1$
		this.httpClient = httpClient;
	}

	protected void hardClose() {
		if (getMethod != null) {
			getMethod.releaseConnection();
			getMethod = null;
		}
		super.hardClose();
	}

	protected Credentials getFileRequestCredentials() throws UnsupportedCallbackException, IOException {
		if (fileRequestConnectContext == null)
			return null;
		final CallbackHandler callbackHandler = fileRequestConnectContext.getCallbackHandler();
		if (callbackHandler == null)
			return null;
		final NameCallback usernameCallback = new NameCallback(USERNAME_PREFIX);
		final ObjectCallback passwordCallback = new ObjectCallback();
		callbackHandler.handle(new Callback[] {usernameCallback, passwordCallback});
		username = usernameCallback.getName();
		password = (String) passwordCallback.getObject();
		return new UsernamePasswordCredentials(username, password);
	}

	private Proxy getSystemProxy() {
		final String systemHttpProxyHost = System.getProperty(HTTP_PROXY_HOST, null);
		final String systemHttpProxyPort = System.getProperty(HTTP_PROXY_PORT, "" //$NON-NLS-1$
				+ HTTP_PORT);
		int port = -1;
		try {
			port = Integer.parseInt(systemHttpProxyPort);
		} catch (final Exception e) {

		}
		if (systemHttpProxyHost == null || systemHttpProxyHost.equals("")) //$NON-NLS-1$
			return null;
		return new Proxy(Proxy.Type.HTTP, new ProxyAddress(systemHttpProxyHost, port));
	}

	protected void setupProxy(String urlString) {
		if (proxy == null)
			proxy = getSystemProxy();
		if (proxy != null && !Proxy.NO_PROXY.equals(proxy) && !urlUsesHttps(urlString)) {
			final ProxyAddress address = proxy.getAddress();
			httpClient.getHostConfiguration().setProxy(getHostFromURL(address.getHostName()), address.getPort());
			final String proxyUsername = proxy.getUsername();
			final String proxyPassword = proxy.getPassword();
			if (username != null) {
				final Credentials credentials = new UsernamePasswordCredentials(proxyUsername, proxyPassword);
				final AuthScope proxyAuthScope = new AuthScope(address.getHostName(), address.getPort(), AuthScope.ANY_REALM);
				httpClient.getState().setProxyCredentials(proxyAuthScope, credentials);
			}
		}
	}

	protected void setupAuthentication(String urlString) throws UnsupportedCallbackException, IOException {
		Credentials credentials = null;
		if (username == null) {
			credentials = getFileRequestCredentials();
		}

		if (credentials != null && username != null) {
			final AuthScope authScope = new AuthScope(getHostFromURL(urlString), getPortFromURL(urlString), AuthScope.ANY_REALM);
			httpClient.getState().setCredentials(authScope, credentials);
		}
	}

	protected void setupHostAndPort(String urlString) {
		if (urlUsesHttps(urlString)) {
			final Protocol acceptAllSsl = new Protocol(HTTPS, new SslProtocolSocketFactory(proxy), getPortFromURL(urlString));
			httpClient.getHostConfiguration().setHost(getHostFromURL(urlString), getPortFromURL(urlString), acceptAllSsl);
		} else {
			httpClient.getHostConfiguration().setHost(getHostFromURL(urlString), getPortFromURL(urlString));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.filetransfer.retrieve.AbstractRetrieveFileTransfer#openStreams()
	 */
	protected void openStreams() throws IncomingFileTransferException {
		final String urlString = getRemoteFileURL().toString();

		try {
			httpClient.getHttpConnectionManager().getParams().setSoTimeout(DEFAULT_CONNECTION_TIMEOUT);
			httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT);

			setupProxy(urlString);

			setupAuthentication(urlString);

			setupHostAndPort(urlString);

			getMethod = new GetMethod(urlString);
			getMethod.setFollowRedirects(true);

			final int code = httpClient.executeMethod(getMethod);

			if (code == HttpURLConnection.HTTP_OK) {
				final long contentLength = getMethod.getResponseContentLength();
				setInputStream(getMethod.getResponseBodyAsStream());
				setFileLength(contentLength);

				listener.handleTransferEvent(new IIncomingFileTransferReceiveStartEvent() {
					private static final long serialVersionUID = -59096575294481755L;

					public IFileID getFileID() {
						return remoteFileID;
					}

					public IIncomingFileTransfer receive(File localFileToSave) throws IOException {
						setOutputStream(new BufferedOutputStream(new FileOutputStream(localFileToSave)));
						job = new FileTransferJob(getRemoteFileURL().toString());
						job.schedule();
						return HttpClientRetrieveFileTransfer.this;
					}

					public String toString() {
						final StringBuffer sb = new StringBuffer("IIncomingFileTransferReceiveStartEvent["); //$NON-NLS-1$
						sb.append("isdone=").append(done).append(";"); //$NON-NLS-1$ //$NON-NLS-2$
						sb.append("bytesReceived=").append( //$NON-NLS-1$
								bytesReceived).append("]"); //$NON-NLS-1$
						return sb.toString();
					}

					public void cancel() {
						hardClose();
					}

					public IIncomingFileTransfer receive(OutputStream streamToStore) throws IOException {
						setOutputStream(streamToStore);
						setCloseOutputStream(false);
						job = new FileTransferJob(getRemoteFileURL().toString());
						job.schedule();
						return HttpClientRetrieveFileTransfer.this;
					}

				});
			} else if (code == HttpURLConnection.HTTP_UNAUTHORIZED || code == HttpURLConnection.HTTP_FORBIDDEN) {
				getMethod.getResponseBody();
				// login or reauthenticate due to an expired session
				getMethod.releaseConnection();
				throw new IncomingFileTransferException(Messages.HttpClientRetrieveFileTransfer_Unauthorized);
			} else if (code == HttpURLConnection.HTTP_PROXY_AUTH) {
				throw new LoginException(Messages.HttpClientRetrieveFileTransfer_Proxy_Auth_Required);
			} else {
				throw new IOException("HttpClient connection error response code: " + code); //$NON-NLS-1$
			}
		} catch (final Exception e) {
			throw new IncomingFileTransferException("Could not connect to " //$NON-NLS-1$
					+ urlString, e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.filetransfer.IRetrieveFileTransferContainerAdapter#setConnectContextForAuthentication(org.eclipse.ecf.core.security.IConnectContext)
	 */
	public void setConnectContextForAuthentication(IConnectContext connectContext) {
		this.fileRequestConnectContext = connectContext;
		this.username = null;
		this.password = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.filetransfer.IRetrieveFileTransferContainerAdapter#setProxy(org.eclipse.ecf.core.util.Proxy)
	 */
	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}

	protected static String getHostFromURL(String url) {
		String result = url;
		final int colonSlashSlash = url.indexOf("://"); //$NON-NLS-1$

		if (colonSlashSlash >= 0) {
			result = url.substring(colonSlashSlash + 3);
		}

		final int colonPort = result.indexOf(':');
		final int requestPath = result.indexOf('/');

		int substringEnd;

		if (colonPort > 0 && requestPath > 0)
			substringEnd = Math.min(colonPort, requestPath);
		else if (colonPort > 0)
			substringEnd = colonPort;
		else if (requestPath > 0)
			substringEnd = requestPath;
		else
			substringEnd = result.length();

		return result.substring(0, substringEnd);

	}

	protected static int getPortFromURL(String url) {
		final int colonSlashSlash = url.indexOf("://"); //$NON-NLS-1$
		final int colonPort = url.indexOf(':', colonSlashSlash + 1);
		if (colonPort < 0)
			return urlUsesHttps(url) ? HTTPS_PORT : HTTP_PORT;

		final int requestPath = url.indexOf('/', colonPort + 1);

		int end;
		if (requestPath < 0)
			end = url.length();
		else
			end = requestPath;

		return Integer.parseInt(url.substring(colonPort + 1, end));
	}

	protected static boolean urlUsesHttps(String url) {
		return url.matches(HTTPS + ".*"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.internal.provider.filetransfer.AbstractRetrieveFileTransfer#supportsProtocol(java.lang.String)
	 */
	public static boolean supportsProtocol(String protocolString) {
		for (int i = 0; i < supportedProtocols.length; i++)
			if (supportedProtocols[i].equalsIgnoreCase(protocolString))
				return true;
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.IIdentifiable#getID()
	 */
	public ID getID() {
		return new FileTransferID(getRetrieveNamespace(), getRemoteFileURL());
	}

}
