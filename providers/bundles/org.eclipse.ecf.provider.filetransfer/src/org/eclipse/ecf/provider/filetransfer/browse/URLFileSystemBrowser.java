/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.provider.filetransfer.browse;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import org.eclipse.ecf.core.security.*;
import org.eclipse.ecf.core.util.Proxy;
import org.eclipse.ecf.filetransfer.IRemoteFile;
import org.eclipse.ecf.filetransfer.IRemoteFileSystemListener;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.internal.provider.filetransfer.*;
import org.eclipse.ecf.provider.filetransfer.util.JREProxyHelper;

/**
 *
 */
public class URLFileSystemBrowser extends AbstractFileSystemBrowser {

	private static final String USERNAME_PREFIX = Messages.UrlConnectionRetrieveFileTransfer_USERNAME_PROMPT;

	private static final String JRE_CONNECT_TIMEOUT_PROPERTY = "sun.net.client.defaultConnectTimeout"; //$NON-NLS-1$

	private static final String DEFAULT_CONNECT_TIMEOUT = "30000"; //$NON-NLS-1$

	private static final String JRE_READ_TIMEOUT_PROPERTY = "sun.net.client.defaultReadTimeout"; //$NON-NLS-1$

	private static final String DEFAULT_READ_TIMEOUT = "30000"; //$NON-NLS-1$

	private JREProxyHelper proxyHelper = null;

	protected String username = null;

	protected String password = null;

	/**
	 * @param directoryOrFileID
	 * @param listener
	 */
	public URLFileSystemBrowser(IFileID directoryOrFileID, IRemoteFileSystemListener listener, URL directoryOrFileURL, IConnectContext connectContext, Proxy proxy) {
		super(directoryOrFileID, listener, directoryOrFileURL, connectContext, proxy);
		proxyHelper = new JREProxyHelper();
	}

	private void setupTimeouts() {
		String existingTimeout = System.getProperty(JRE_CONNECT_TIMEOUT_PROPERTY);
		if (existingTimeout == null) {
			System.setProperty(JRE_CONNECT_TIMEOUT_PROPERTY, DEFAULT_CONNECT_TIMEOUT);
		}
		existingTimeout = System.getProperty(JRE_READ_TIMEOUT_PROPERTY);
		if (existingTimeout == null) {
			System.setProperty(JRE_READ_TIMEOUT_PROPERTY, DEFAULT_READ_TIMEOUT);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.filetransfer.browse.AbstractFileSystemBrowser#runRequest()
	 */
	protected void runRequest() throws Exception {
		setupProxies();
		setupAuthentication();
		setupTimeouts();
		URLConnection urlConnection = directoryOrFile.openConnection();
		// set cache to off if using jar protocol
		// this is for addressing bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=235933
		if (directoryOrFile.getProtocol().equalsIgnoreCase("jar")) { //$NON-NLS-1$
			urlConnection.setUseCaches(false);
		}
		// Add http 1.1 'Connection: close' header in order to potentially avoid
		// server issue described here https://bugs.eclipse.org/bugs/show_bug.cgi?id=234916#c13
		// See bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=247197
		// also see http 1.1 rfc section 14-10 in http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
		urlConnection.setRequestProperty("Connection", "close"); //$NON-NLS-1$ //$NON-NLS-2$

		IURLConnectionModifier connectionModifier = Activator.getDefault().getURLConnectionModifier();
		if (connectionModifier != null) {
			connectionModifier.setSocketFactoryForConnection(urlConnection);
		}
		InputStream ins = urlConnection.getInputStream();
		ins.close();
		remoteFiles = new IRemoteFile[1];
		remoteFiles[0] = new URLRemoteFile(urlConnection.getLastModified(), urlConnection.getContentLength(), fileID);
	}

	protected void setupAuthentication() throws IOException, UnsupportedCallbackException {
		if (connectContext == null)
			return;
		final CallbackHandler callbackHandler = connectContext.getCallbackHandler();
		if (callbackHandler == null)
			return;
		final NameCallback usernameCallback = new NameCallback(USERNAME_PREFIX);
		final ObjectCallback passwordCallback = new ObjectCallback();
		// Call callback with username and password callbacks
		callbackHandler.handle(new Callback[] {usernameCallback, passwordCallback});
		username = usernameCallback.getName();
		Object o = passwordCallback.getObject();
		if (!(o instanceof String))
			throw new UnsupportedCallbackException(passwordCallback, Messages.UrlConnectionRetrieveFileTransfer_UnsupportedCallbackException);
		password = (String) passwordCallback.getObject();
		// Now set authenticator to our authenticator with user and password
		Authenticator.setDefault(new UrlConnectionAuthenticator());
	}

	class UrlConnectionAuthenticator extends Authenticator {
		/* (non-Javadoc)
		 * @see java.net.Authenticator#getPasswordAuthentication()
		 */
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(username, password.toCharArray());
		}
	}

	protected void setupProxy(final Proxy proxy2) {
		proxyHelper.setupProxy(proxy2);
	}

}
