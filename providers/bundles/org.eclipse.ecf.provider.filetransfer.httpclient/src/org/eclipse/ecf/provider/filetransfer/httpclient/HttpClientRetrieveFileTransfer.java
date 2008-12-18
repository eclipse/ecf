/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Composent, Inc. - initial API and implementation
 * 				 Maarten Meijer - bug 237936, added gzip encoded transfer default
 ******************************************************************************/
package org.eclipse.ecf.provider.filetransfer.httpclient;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.security.auth.login.LoginException;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.util.DateUtil;
import org.eclipse.core.runtime.*;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.security.*;
import org.eclipse.ecf.core.util.Proxy;
import org.eclipse.ecf.core.util.ProxyAddress;
import org.eclipse.ecf.filetransfer.*;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.internal.provider.filetransfer.httpclient.*;
import org.eclipse.ecf.provider.filetransfer.identity.FileTransferID;
import org.eclipse.ecf.provider.filetransfer.retrieve.AbstractRetrieveFileTransfer;
import org.eclipse.ecf.provider.filetransfer.retrieve.HttpHelper;
import org.eclipse.ecf.provider.filetransfer.util.JREProxyHelper;
import org.eclipse.osgi.util.NLS;

public class HttpClientRetrieveFileTransfer extends AbstractRetrieveFileTransfer {

	/**
	 * gzip encoding wrapper for httpclient class. Copied from Mylyn project, bug 205708
	 *
	 */
	public class GzipGetMethod extends GetMethod {

		private static final String CONTENT_ENCODING = "Content-Encoding"; //$NON-NLS-1$
		private static final String ACCEPT_ENCODING = "Accept-encoding"; //$NON-NLS-1$
		private static final String CONTENT_ENCODING_GZIP = "gzip"; //$NON-NLS-1$

		private static final String CONTENT_ENCODING_ACCEPTED = CONTENT_ENCODING_GZIP;

		private boolean gzipReceived = false;

		public GzipGetMethod(String urlString) {
			super(urlString);
		}

		private boolean isZippedReply() {
			boolean zipped = (null != this.getResponseHeader(CONTENT_ENCODING) && this.getResponseHeader(CONTENT_ENCODING).getValue().equals(CONTENT_ENCODING_GZIP));
			// XXX removed see https://bugs.eclipse.org/bugs/show_bug.cgi?id=237936#c34
			// apache can also insert something after a 302 redirect
			//		|| (null != this.getResponseHeader(CONTENT_TYPE) && this.getResponseHeader(CONTENT_TYPE).getValue().equals(APPLICATION_X_GZIP));
			return zipped;
		}

		public int execute(HttpState state, HttpConnection conn) throws HttpException, IOException {
			// Insert accept-encoding header
			this.setRequestHeader(ACCEPT_ENCODING, CONTENT_ENCODING_ACCEPTED);
			int result = super.execute(state, conn);
			// test what is sent back
			if (isZippedReply()) {
				gzipReceived = true;
			}
			return result;
		}

		public InputStream getResponseBodyAsUnzippedStream() throws IOException {
			InputStream input = super.getResponseBodyAsStream();
			try {
				if (gzipReceived) {
					// extract on the fly
					return new java.util.zip.GZIPInputStream(input);
				}
			} catch (IOException e) {
				Activator.getDefault().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, IStatus.WARNING, "Exception creating gzip input stream", e)); //$NON-NLS-1$ 
				throw e;
			}
			return input;
		}

	}

	private static final String USERNAME_PREFIX = Messages.HttpClientRetrieveFileTransfer_Username_Prefix;

	protected static final int DEFAULT_CONNECTION_TIMEOUT = 30000;

	protected static final int HTTP_PORT = 80;

	protected static final int HTTPS_PORT = 443;

	protected static final int MAX_RETRY = 2;

	protected static final String HTTPS = Messages.FileTransferNamespace_Https_Protocol;

	protected static final String HTTP = Messages.FileTransferNamespace_Http_Protocol;

	protected static final String[] supportedProtocols = {HTTP, HTTPS};

	private static final String LAST_MODIFIED_HEADER = "Last-Modified"; //$NON-NLS-1$

	private GzipGetMethod getMethod = null;

	private HttpClient httpClient = null;

	private String username;

	private String password;

	private int responseCode = -1;

	private String remoteFileName;

	protected int httpVersion = 1;

	protected IFileID fileid = null;

	protected JREProxyHelper proxyHelper = null;

	public HttpClientRetrieveFileTransfer(HttpClient httpClient) {
		this.httpClient = httpClient;
		Assert.isNotNull(this.httpClient);
		proxyHelper = new JREProxyHelper();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.filetransfer.retrieve.AbstractRetrieveFileTransfer#getRemoteFileName()
	 */
	public String getRemoteFileName() {
		return remoteFileName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ecf.provider.filetransfer.retrieve.AbstractRetrieveFileTransfer#hardClose()
	 */
	protected void hardClose() {
		super.hardClose();
		if (getMethod != null) {
			getMethod.releaseConnection();
			getMethod = null;
		}
		responseCode = -1;
		if (proxyHelper != null) {
			proxyHelper.dispose();
			proxyHelper = null;
		}
	}

	protected Credentials getFileRequestCredentials() throws UnsupportedCallbackException, IOException {
		if (connectContext == null)
			return null;
		final CallbackHandler callbackHandler = connectContext.getCallbackHandler();
		if (callbackHandler == null)
			return null;
		final NameCallback usernameCallback = new NameCallback(USERNAME_PREFIX);
		final ObjectCallback passwordCallback = new ObjectCallback();
		callbackHandler.handle(new Callback[] {usernameCallback, passwordCallback});
		username = usernameCallback.getName();
		password = (String) passwordCallback.getObject();
		return new UsernamePasswordCredentials(username, password);
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
			ISSLSocketFactoryModifier sslSocketFactoryModifier = Activator.getDefault().getSSLSocketFactoryModifier();
			Protocol sslProtocol = null;
			ProtocolSocketFactory psf = null;
			if (sslSocketFactoryModifier != null) {
				psf = sslSocketFactoryModifier.getProtocolSocketFactoryForProxy(proxy);
			} else {
				psf = new HttpClientSslProtocolSocketFactory(proxy);
			}
			sslProtocol = new Protocol(HTTPS, psf, getPortFromURL(urlString));
			Protocol.registerProtocol(HTTPS, sslProtocol);
			httpClient.getHostConfiguration().setHost(getHostFromURL(urlString), getPortFromURL(urlString), sslProtocol);
		} else {
			httpClient.getHostConfiguration().setHost(getHostFromURL(urlString), getPortFromURL(urlString));
		}
	}

	protected void setRequestHeaderValues() throws InvalidFileRangeSpecificationException {
		final IFileRangeSpecification rangeSpec = getFileRangeSpecification();
		if (rangeSpec != null) {
			final long startPosition = rangeSpec.getStartPosition();
			final long endPosition = rangeSpec.getEndPosition();
			if (startPosition < 0)
				throw new InvalidFileRangeSpecificationException(Messages.HttpClientRetrieveFileTransfer_RESUME_START_POSITION_LESS_THAN_ZERO, rangeSpec);
			if (endPosition != -1L && endPosition <= startPosition)
				throw new InvalidFileRangeSpecificationException(Messages.HttpClientRetrieveFileTransfer_RESUME_ERROR_END_POSITION_LESS_THAN_START, rangeSpec);
			setRangeHeader("bytes=" + startPosition + "-" + ((endPosition == -1L) ? "" : ("" + endPosition))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		// set max-age for cache control to 0 for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=249990
		getMethod.addRequestHeader("Cache-Control", "max-age=0"); //$NON-NLS-1$//$NON-NLS-2$
	}

	private void setRangeHeader(String value) {
		getMethod.addRequestHeader("Range", value); //$NON-NLS-1$
	}

	private boolean isHTTP11() {
		return (httpVersion >= 1);
	}

	public int getResponseCode() {
		if (responseCode != -1)
			return responseCode;
		HttpVersion version = getMethod.getEffectiveVersion();
		if (version == null) {
			responseCode = -1;
			httpVersion = 1;
			return responseCode;
		}
		httpVersion = version.getMinor();
		responseCode = getMethod.getStatusCode();
		return responseCode;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ecf.core.identity.IIdentifiable#getID()
	 */
	public ID getID() {
		return fileid;
	}

	private long getLastModifiedTimeFromHeader() throws IOException {
		Header lastModifiedHeader = getMethod.getResponseHeader(LAST_MODIFIED_HEADER);
		if (lastModifiedHeader == null)
			throw new IOException(Messages.HttpClientRetrieveFileTransfer_INVALID_LAST_MODIFIED_TIME);

		String lastModifiedString = lastModifiedHeader.getValue();
		long lastModified = 0;
		if (lastModifiedString != null) {
			try {
				lastModified = DateUtil.parseDate(lastModifiedString).getTime();
			} catch (Exception e) {
				throw new IOException(Messages.HttpClientRetrieveFileTransfer_EXCEPITION_INVALID_LAST_MODIFIED_FROM_SERVER);
			}
		}
		return lastModified;
	}

	protected void getResponseHeaderValues() throws IOException {
		if (getResponseCode() == -1)
			throw new IOException(Messages.HttpClientRetrieveFileTransfer_INVALID_SERVER_RESPONSE_TO_PARTIAL_RANGE_REQUEST);
		Header lastModifiedHeader = getMethod.getResponseHeader(LAST_MODIFIED_HEADER);
		if (lastModifiedHeader != null) {
			setLastModifiedTime(getLastModifiedTimeFromHeader());
		}
		setFileLength(getMethod.getResponseContentLength());
		fileid = new FileTransferID(getRetrieveNamespace(), getRemoteFileURL());

		// Get content disposition header and get remote file name from it if possible.
		Header contentDispositionHeader = getMethod.getResponseHeader(HttpHelper.CONTENT_DISPOSITION_HEADER);
		if (contentDispositionHeader != null) {
			remoteFileName = HttpHelper.getRemoteFileNameFromContentDispositionHeader(contentDispositionHeader.getValue());
		}
		// If still null, get the path from httpclient.getMethod()
		if (remoteFileName == null) {
			// No name could be extracted using Content-Disposition. Let's try the
			// path from the getMethod.
			String pathStr = getMethod.getPath();
			if (pathStr != null && pathStr.length() > 0) {
				IPath path = Path.fromPortableString(pathStr);
				if (path.segmentCount() > 0)
					remoteFileName = path.lastSegment();
			}
			// If still null, use the input file name
			if (remoteFileName == null)
				// Last resort. Use the path of the initial URL request
				remoteFileName = super.getRemoteFileName();
		}
	}

	final class ECFCredentialsProvider implements CredentialsProvider {

		/**
		 * @throws CredentialsNotAvailableException  
		 */
		public Credentials getCredentials(AuthScheme scheme, String host, int port, boolean isProxyAuthenticating) throws CredentialsNotAvailableException {
			if ("ntlm".equalsIgnoreCase(scheme.getSchemeName())) { //$NON-NLS-1$
				return createNTLMCredentials(getProxy());
			}
			return null;
		}
	}

	Proxy getProxy() {
		return proxy;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.filetransfer.retrieve.AbstractRetrieveFileTransfer#openStreams()
	 */
	protected void openStreams() throws IncomingFileTransferException {
		final String urlString = getRemoteFileURL().toString();

		int code = -1;

		try {
			httpClient.getHttpConnectionManager().getParams().setSoTimeout(DEFAULT_CONNECTION_TIMEOUT);
			httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT);

			setupAuthentication(urlString);

			setupHostAndPort(urlString);

			getMethod = new GzipGetMethod(urlString);
			getMethod.setFollowRedirects(true);
			// Define a CredentialsProvider - found that possibility while debugging in org.apache.commons.httpclient.HttpMethodDirector.processProxyAuthChallenge(HttpMethod)
			// Seems to be another way to select the credentials.
			getMethod.getParams().setParameter(CredentialsProvider.PROVIDER, new ECFCredentialsProvider());
			setRequestHeaderValues();

			code = httpClient.executeMethod(getMethod);

			if (code == HttpURLConnection.HTTP_PARTIAL || code == HttpURLConnection.HTTP_OK) {
				getResponseHeaderValues();
				setInputStream(getMethod.getResponseBodyAsUnzippedStream());
				fireReceiveStartEvent();
			} else if (code == HttpURLConnection.HTTP_NOT_FOUND) {
				getMethod.releaseConnection();
				throw new FileNotFoundException(urlString);
			} else if (code == HttpURLConnection.HTTP_UNAUTHORIZED || code == HttpURLConnection.HTTP_FORBIDDEN) {
				getMethod.getResponseBody();
				getMethod.releaseConnection();
				throw new IncomingFileTransferException(Messages.HttpClientRetrieveFileTransfer_Unauthorized);
			} else if (code == HttpURLConnection.HTTP_PROXY_AUTH) {
				getMethod.releaseConnection();
				throw new LoginException(Messages.HttpClientRetrieveFileTransfer_Proxy_Auth_Required);
			} else {
				getMethod.releaseConnection();
				throw new IOException(NLS.bind(Messages.HttpClientRetrieveFileTransfer_ERROR_GENERAL_RESPONSE_CODE, new Integer(code)));
			}
		} catch (final Exception e) {
			throw new IncomingFileTransferException(NLS.bind(Messages.HttpClientRetrieveFileTransfer_EXCEPTION_COULD_NOT_CONNECT, urlString), e, code);
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ecf.filetransfer.IRetrieveFileTransferContainerAdapter#setConnectContextForAuthentication(org.eclipse.ecf.core.security.IConnectContext)
	 */
	public void setConnectContextForAuthentication(IConnectContext connectContext) {
		super.setConnectContextForAuthentication(connectContext);
		this.username = null;
		this.password = null;
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

	protected boolean isConnected() {
		return (getMethod != null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ecf.provider.filetransfer.retrieve.AbstractRetrieveFileTransfer#doPause()
	 */
	protected boolean doPause() {
		if (isPaused() || !isConnected() || isDone())
			return false;
		this.paused = true;
		return this.paused;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ecf.provider.filetransfer.retrieve.AbstractRetrieveFileTransfer#doResume()
	 */
	protected boolean doResume() {
		if (!isPaused() || isConnected())
			return false;
		return openStreamsForResume();
	}

	protected void setResumeRequestHeaderValues() throws IOException {
		if (this.bytesReceived <= 0 || this.fileLength <= this.bytesReceived)
			throw new IOException(Messages.HttpClientRetrieveFileTransfer_RESUME_START_ERROR);
		setRangeHeader("bytes=" + this.bytesReceived + "-"); //$NON-NLS-1$ //$NON-NLS-2$
		// set max-age for cache control to 0 for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=249990
		getMethod.addRequestHeader("Cache-Control", "max-age=0"); //$NON-NLS-1$//$NON-NLS-2$
	}

	private boolean openStreamsForResume() {
		final URL theURL = getRemoteFileURL();

		int code = -1;

		try {
			remoteFileURL = new URL(theURL.toString());
			final String urlString = getRemoteFileURL().toString();

			httpClient.getHttpConnectionManager().getParams().setSoTimeout(DEFAULT_CONNECTION_TIMEOUT);
			httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT);

			setupAuthentication(urlString);

			setupHostAndPort(urlString);

			getMethod = new GzipGetMethod(urlString);
			getMethod.setFollowRedirects(true);

			setResumeRequestHeaderValues();

			code = httpClient.executeMethod(getMethod);

			if (code == HttpURLConnection.HTTP_PARTIAL || code == HttpURLConnection.HTTP_OK) {
				getResumeResponseHeaderValues();
				setInputStream(getMethod.getResponseBodyAsUnzippedStream());
				this.paused = false;
				fireReceiveResumedEvent();
			} else if (code == HttpURLConnection.HTTP_NOT_FOUND) {
				getMethod.releaseConnection();
				throw new FileNotFoundException(urlString);
			} else if (code == HttpURLConnection.HTTP_UNAUTHORIZED || code == HttpURLConnection.HTTP_FORBIDDEN) {
				getMethod.getResponseBody();
				getMethod.releaseConnection();
				throw new IncomingFileTransferException(Messages.HttpClientRetrieveFileTransfer_Unauthorized, code);
			} else if (code == HttpURLConnection.HTTP_PROXY_AUTH) {
				getMethod.releaseConnection();
				throw new LoginException(Messages.HttpClientRetrieveFileTransfer_Proxy_Auth_Required);
			} else {
				getMethod.releaseConnection();
				throw new IOException(NLS.bind(Messages.HttpClientRetrieveFileTransfer_ERROR_GENERAL_RESPONSE_CODE, new Integer(code)));
			}
			return true;
		} catch (final Exception e) {
			this.exception = e;
			this.done = true;
			hardClose();
			fireTransferReceiveDoneEvent();
			return false;
		}
	}

	protected void getResumeResponseHeaderValues() throws IOException {
		if (getResponseCode() != HttpURLConnection.HTTP_PARTIAL)
			throw new IOException();
		if (lastModifiedTime != getLastModifiedTimeFromHeader())
			throw new IOException(Messages.HttpClientRetrieveFileTransfer_EXCEPTION_FILE_MODIFIED_SINCE_LAST_ACCESS);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ecf.provider.filetransfer.retrieve.AbstractRetrieveFileTransfer#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == null)
			return null;
		if (adapter.equals(IFileTransferPausable.class) && isHTTP11())
			return this;
		return super.getAdapter(adapter);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.filetransfer.retrieve.AbstractRetrieveFileTransfer#setupProxy(org.eclipse.ecf.core.util.Proxy)
	 */
	protected void setupProxy(Proxy proxy) {
		if (proxy.getType().equals(Proxy.Type.HTTP)) {
			final ProxyAddress address = proxy.getAddress();
			httpClient.getHostConfiguration().setProxy(getHostFromURL(address.getHostName()), address.getPort());
			final String proxyUsername = proxy.getUsername();
			final String proxyPassword = proxy.getPassword();
			if (proxyUsername != null) {
				final Credentials credentials = new UsernamePasswordCredentials(proxyUsername, proxyPassword);
				final AuthScope proxyAuthScope = new AuthScope(address.getHostName(), address.getPort(), AuthScope.ANY_REALM);
				httpClient.getState().setProxyCredentials(proxyAuthScope, credentials);
			}
		} else if (proxy.getType().equals(Proxy.Type.SOCKS)) {
			proxyHelper.setupProxy(proxy);
		}
	}

	protected NTCredentials createNTLMCredentials(Proxy p) {
		if (p == null) {
			return null;
		}
		String un = getNTLMUserName(p);
		String domain = getNTLMDomainName(p);
		if (un == null || domain == null)
			return null;
		return new NTCredentials(un, p.getPassword(), p.getAddress().getHostName(), domain);
	}

	protected String getNTLMDomainName(Proxy p) {
		String domainUsername = p.getUsername();
		int slashloc = domainUsername.indexOf('\\');
		if (slashloc == -1)
			return null;
		return domainUsername.substring(0, slashloc);
	}

	protected String getNTLMUserName(Proxy p) {
		String domainUsername = p.getUsername();
		int slashloc = domainUsername.indexOf('\\');
		if (slashloc == -1)
			return null;
		return domainUsername.substring(slashloc + 1);
	}

}
