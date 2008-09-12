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
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.ecf.core.security.*;
import org.eclipse.ecf.core.util.Proxy;
import org.eclipse.ecf.core.util.ProxyAddress;
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

	URL directoryOrFile;

	IConnectContext connectContext;
	Proxy proxy;

	private JREProxyHelper proxyHelper = null;

	protected String username = null;

	protected String password = null;

	/**
	 * @param directoryOrFileID
	 * @param listener
	 */
	public URLFileSystemBrowser(IFileID directoryOrFileID, IRemoteFileSystemListener listener, URL directoryOrFileURL, IConnectContext connectContext, Proxy proxy) {
		super(directoryOrFileID, listener);
		this.directoryOrFile = directoryOrFileURL;
		this.connectContext = connectContext;
		this.proxy = proxy;
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
		remoteFiles[0] = new URLRemoteFile(urlConnection, fileID);
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

	/**
	 * Select a single proxy from a set of proxies available for the given host.  This implementation
	 * selects in the following manner:  1) If proxies provided is null or array of 0 length, null 
	 * is returned.  If only one proxy is available (array of length 1) then the entry is returned.
	 * If proxies provided is length > 1, then if the type of a proxy in the array matches the given
	 * protocol (e.g. http, https), then the first matching proxy is returned.  If the protocol does
	 * not match any of the proxies, then the *first* proxy (i.e. proxies[0]) is returned.  Subclasses may
	 * override if desired.
	 * 
	 * @param protocol the target protocol (e.g. http, https, scp, etc).  Will not be <code>null</code>.
	 * @param proxies the proxies to select from.  May be <code>null</code> or array of length 0.
	 * @return proxy data selected from the proxies provided.  
	 */
	protected IProxyData selectProxyFromProxies(String protocol, IProxyData[] proxies) {
		if (proxies == null || proxies.length == 0)
			return null;
		// If only one proxy is available, then use that
		if (proxies.length == 1)
			return proxies[0];
		// If more than one proxy is available, then if http/https protocol then look for that
		// one...if not found then use first
		if (protocol.equalsIgnoreCase("http")) { //$NON-NLS-1$
			for (int i = 0; i < proxies.length; i++) {
				if (proxies[i].getType().equals(IProxyData.HTTP_PROXY_TYPE))
					return proxies[i];
			}
		} else if (protocol.equalsIgnoreCase("https")) { //$NON-NLS-1$
			for (int i = 0; i < proxies.length; i++) {
				if (proxies[i].getType().equals(IProxyData.HTTPS_PROXY_TYPE))
					return proxies[i];
			}
		}
		// If we haven't found it yet, then return the first one.
		return proxies[0];
	}

	protected void setupProxies() {
		// If it's been set directly (via ECF API) then this overrides platform settings
		if (proxy == null) {
			try {
				IProxyService proxyService = Activator.getDefault().getProxyService();
				// Only do this if platform service exists
				if (proxyService != null) {
					// Setup via proxyService entry
					URL target = directoryOrFile;
					final IProxyData[] proxies = proxyService.getProxyDataForHost(target.getHost());
					IProxyData selectedProxy = selectProxyFromProxies(target.getProtocol(), proxies);
					if (selectedProxy != null) {
						proxy = new Proxy(((selectedProxy.getType().equalsIgnoreCase(IProxyData.SOCKS_PROXY_TYPE)) ? Proxy.Type.SOCKS : Proxy.Type.HTTP), new ProxyAddress(selectedProxy.getHost(), selectedProxy.getPort()), selectedProxy.getUserId(), selectedProxy.getPassword());
					}
				}

			} catch (Exception e) {
				// If we don't even have the classes for this (i.e. the org.eclipse.core.net plugin not available)
				// then we simply log and ignore
				Activator.logNoProxyWarning(e);
			} catch (NoClassDefFoundError e) {
				Activator.logNoProxyWarning(e);
			}
		}
		if (proxy != null)
			setupProxy(proxy);

	}

}
