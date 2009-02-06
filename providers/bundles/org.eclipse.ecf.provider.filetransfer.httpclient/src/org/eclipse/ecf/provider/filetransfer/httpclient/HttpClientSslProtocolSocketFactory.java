/****************************************************************************
 * Copyright (c) 2004, 2009 Composent, Inc., IBM and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Composent, Inc. - initial API and implementation
 *  Henrich Kraemer - bug 263869, testHttpsReceiveFile fails using HTTP proxy
 *****************************************************************************/

package org.eclipse.ecf.provider.filetransfer.httpclient;

import java.io.IOException;
import java.net.*;
import javax.net.ssl.*;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;

/**
 *
 */
public class HttpClientSslProtocolSocketFactory implements SecureProtocolSocketFactory {
	public static final String DEFAULT_SSL_PROTOCOL = "https.protocols"; //$NON-NLS-1$

	private SSLContext sslContext = null;

	private String defaultProtocolNames = System.getProperty(DEFAULT_SSL_PROTOCOL);

	public HttpClientSslProtocolSocketFactory() {
		// empty
	}

	private SSLSocketFactory getSSLSocketFactory() throws IOException {
		if (null == sslContext) {
			try {
				sslContext = getSSLContext(defaultProtocolNames);
			} catch (Exception e) {
				IOException ioe = new IOException();
				ioe.initCause(e);
				throw ioe;
			}
		}
		return (sslContext == null) ? (SSLSocketFactory) SSLSocketFactory.getDefault() : sslContext.getSocketFactory();
	}

	public SSLContext getSSLContext(String protocols) {
		SSLContext rtvContext = null;

		if (protocols != null) {
			String protocolNames[] = protocols.split(","); //$NON-NLS-1$
			for (int i = 0; i < protocolNames.length; i++) {
				try {
					rtvContext = SSLContext.getInstance(protocolNames[i]);
					sslContext.init(null, new TrustManager[] {new HttpClientSslTrustManager()}, null);
					break;
				} catch (Exception e) {
					// just continue
				}
			}
		}
		return rtvContext;
	}

	public Socket createSocket(String remoteHost, int remotePort) throws IOException, UnknownHostException {
		return getSSLSocketFactory().createSocket(remoteHost, remotePort);
	}

	public Socket createSocket(String remoteHost, int remotePort, InetAddress clientHost, int clientPort) throws IOException, UnknownHostException {
		return getSSLSocketFactory().createSocket(remoteHost, remotePort, clientHost, clientPort);
	}

	public Socket createSocket(String remoteHost, int remotePort, InetAddress clientHost, int clientPort, HttpConnectionParams params) throws IOException, UnknownHostException, ConnectTimeoutException {
		if (params == null || params.getConnectionTimeout() == 0)
			return getSSLSocketFactory().createSocket(remoteHost, remotePort, clientHost, clientPort);

		Socket socket = getSSLSocketFactory().createSocket();
		socket.bind(new InetSocketAddress(clientHost, clientPort));
		socket.connect(new InetSocketAddress(remoteHost, remotePort), params.getConnectionTimeout());
		return socket;
	}

	public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
		return getSSLSocketFactory().createSocket(socket, host, port, autoClose);
	}

}
