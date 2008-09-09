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

package org.eclipse.ecf.internal.provider.filetransfer.httpclient.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.ProxyClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.eclipse.ecf.core.util.Proxy;
import org.eclipse.ecf.core.util.ProxyAddress;
import org.eclipse.ecf.internal.provider.filetransfer.httpclient.ISSLSocketFactoryModifier;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 *
 */
public class SSLSocketFactoryModifier implements ISSLSocketFactoryModifier, ProtocolSocketFactory {

	private BundleContext context;
	private ServiceTracker sslSocketFactoryTracker;
	private Proxy proxy;

	public void init(BundleContext context) {
		this.context = context;
	}

	private SSLSocketFactory getSSLSocketFactory() {
		if (context == null)
			return null;
		if (sslSocketFactoryTracker == null) {
			sslSocketFactoryTracker = new ServiceTracker(this.context, SSLSocketFactory.class.getName(), null);
			sslSocketFactoryTracker.open();
		}
		return (SSLSocketFactory) sslSocketFactoryTracker.getService();
	}

	public void dispose() {
		if (sslSocketFactoryTracker != null) {
			sslSocketFactoryTracker.close();
			sslSocketFactoryTracker = null;
		}
		this.context = null;
		this.proxy = null;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.httpclient.protocol.ProtocolSocketFactory#createSocket(java.lang.String, int)
	 */
	public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
		final SSLSocketFactory factory = getSSLSocketFactory();
		if (factory == null)
			throw new IOException("Cannot get socket factory");
		return factory.createSocket(host, port);
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.httpclient.protocol.ProtocolSocketFactory#createSocket(java.lang.String, int, java.net.InetAddress, int)
	 */
	public Socket createSocket(String host, int port, InetAddress localAddress, int localPort) throws IOException, UnknownHostException {
		final SSLSocketFactory factory = getSSLSocketFactory();
		if (factory == null)
			throw new IOException("Cannot get socket factory");
		return factory.createSocket(host, port, localAddress, localPort);
	}

	public Socket createSocket(String remoteHost, int remotePort, InetAddress clientHost, int clientPort, HttpConnectionParams params) throws IOException, UnknownHostException, ConnectTimeoutException {
		final SSLSocketFactory factory = getSSLSocketFactory();
		if (factory == null)
			throw new IOException("Cannot get socket factory");
		if (params == null || params.getConnectionTimeout() == 0)
			return factory.createSocket(remoteHost, remotePort, clientHost, clientPort);

		if (proxy != null && !Proxy.NO_PROXY.equals(proxy)) {
			final ProxyClient proxyClient = new ProxyClient();

			final ProxyAddress address = proxy.getAddress();
			proxyClient.getHostConfiguration().setProxy(address.getHostName(), address.getPort());
			proxyClient.getHostConfiguration().setHost(remoteHost, remotePort);
			final String proxyUsername = proxy.getUsername();
			final String proxyPassword = proxy.getPassword();
			if (proxyUsername != null && !proxyUsername.equals("")) { //$NON-NLS-1$
				final Credentials credentials = new UsernamePasswordCredentials(proxyUsername, proxyPassword);
				final AuthScope proxyAuthScope = new AuthScope(address.getHostName(), address.getPort(), AuthScope.ANY_REALM);
				proxyClient.getState().setProxyCredentials(proxyAuthScope, credentials);
			}

			final ProxyClient.ConnectResponse response = proxyClient.connect();
			if (response.getSocket() != null) {
				// tunnel SSL via the resultant socket
				final Socket sslsocket = factory.createSocket(response.getSocket(), remoteHost, remotePort, true);
				return sslsocket;
			}
		}
		// Direct connection
		final Socket socket = factory.createSocket();
		socket.bind(new InetSocketAddress(clientHost, clientPort));
		socket.connect(new InetSocketAddress(remoteHost, remotePort), params.getConnectionTimeout());
		return socket;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.internal.provider.filetransfer.httpclient.ISSLSocketFactoryModifier#getProtocolSocketFactoryForProxy(org.eclipse.ecf.core.util.Proxy)
	 */
	public ProtocolSocketFactory getProtocolSocketFactoryForProxy(Proxy proxy) {
		this.proxy = proxy;
		return this;
	}

}
