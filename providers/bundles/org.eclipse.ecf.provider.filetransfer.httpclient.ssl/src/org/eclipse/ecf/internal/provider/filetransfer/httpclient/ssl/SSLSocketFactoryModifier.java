/****************************************************************************
 * Copyright (c) 2008, 2009 Composent, Inc., IBM and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *    Henrich Kraemer - bug 263869, testHttpsReceiveFile fails using HTTP proxy
 *****************************************************************************/

package org.eclipse.ecf.internal.provider.filetransfer.httpclient.ssl;

import java.io.IOException;
import java.net.*;
import javax.net.ssl.SSLSocketFactory;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.eclipse.ecf.internal.provider.filetransfer.httpclient.Activator;
import org.eclipse.ecf.internal.provider.filetransfer.httpclient.ISSLSocketFactoryModifier;

/**
 *
 */
public class SSLSocketFactoryModifier implements ISSLSocketFactoryModifier, SecureProtocolSocketFactory {

	public void dispose() {
		Protocol.unregisterProtocol("https"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.httpclient.protocol.ProtocolSocketFactory#createSocket(java.lang.String, int)
	 */
	public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
		final SSLSocketFactory factory = getSSLSocketFactory();
		return factory.createSocket(host, port);
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.httpclient.protocol.ProtocolSocketFactory#createSocket(java.lang.String, int, java.net.InetAddress, int)
	 */
	public Socket createSocket(String host, int port, InetAddress localAddress, int localPort) throws IOException, UnknownHostException {
		final SSLSocketFactory factory = getSSLSocketFactory();
		return factory.createSocket(host, port, localAddress, localPort);
	}

	private SSLSocketFactory getSSLSocketFactory() throws IOException {
		final SSLSocketFactory factory = Activator.getDefault().getSSLSocketFactory();
		if (factory == null)
			throw new IOException("Cannot get socket factory"); //$NON-NLS-1$
		return factory;
	}

	public Socket createSocket(String remoteHost, int remotePort, InetAddress clientHost, int clientPort, HttpConnectionParams params) throws IOException, UnknownHostException, ConnectTimeoutException {
		final SSLSocketFactory factory = getSSLSocketFactory();
		if (params == null || params.getConnectionTimeout() == 0)
			return factory.createSocket(remoteHost, remotePort, clientHost, clientPort);

		final Socket socket = factory.createSocket(remoteHost, remotePort, clientHost, clientPort);
		// in httpclient, it seems like they will set the time out for you
		//		socket.connect(new InetSocketAddress(remoteHost, remotePort), params.getConnectionTimeout());
		return socket;
	}

	public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
		final SSLSocketFactory factory = getSSLSocketFactory();
		return factory.createSocket(socket, host, port, autoClose);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.internal.provider.filetransfer.httpclient.ISSLSocketFactoryModifier#getProtocolSocketFactory()
	 */
	public SecureProtocolSocketFactory getProtocolSocketFactory() {
		return this;
	}

}
