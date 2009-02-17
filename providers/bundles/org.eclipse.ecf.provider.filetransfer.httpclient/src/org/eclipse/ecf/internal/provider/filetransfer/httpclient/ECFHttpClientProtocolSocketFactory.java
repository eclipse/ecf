/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.internal.provider.filetransfer.httpclient;

import java.io.IOException;
import java.net.*;
import javax.net.SocketFactory;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ecf.filetransfer.events.socketfactory.INonconnectedSocketFactory;

public class ECFHttpClientProtocolSocketFactory implements ProtocolSocketFactory {

	protected IAdaptable source;
	private INonconnectedSocketFactory unconnectedFactory;
	private ISocketConnectionCallback socketConnectCallback;

	private static final ISocketConnectionCallback NULL_CONNECT_CALLBACK = new ISocketConnectionCallback() {

		public void onSocketConnected(Socket socket) {
			//empty
		}

		public void onSocketConnectionFailed(Socket socket, IOException e) {
			//empty
		}

		public void onSocketCreated(Socket socket) {
			//empty
		}

	};

	public ECFHttpClientProtocolSocketFactory(INonconnectedSocketFactory unconnectedFactory, IAdaptable source, ISocketConnectionCallback socketConnectCallback) {
		super();
		Assert.isNotNull(unconnectedFactory);
		Assert.isNotNull(source);
		this.unconnectedFactory = unconnectedFactory;
		this.source = source;
		this.socketConnectCallback = socketConnectCallback != null ? socketConnectCallback : NULL_CONNECT_CALLBACK;
	}

	public ECFHttpClientProtocolSocketFactory(final SocketFactory socketFactory, IAdaptable source, ISocketConnectionCallback socketConnectCallback) {
		this(new INonconnectedSocketFactory() {
			public Socket createSocket() throws IOException {
				return socketFactory.createSocket();
			}

		}, source, socketConnectCallback);
	}

	public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort) throws IOException, UnknownHostException {

		InetSocketAddress remoteInetAddress = new InetSocketAddress(host, port);
		InetSocketAddress localInetAddress = new InetSocketAddress(clientHost, clientPort);
		return createSocket(remoteInetAddress, localInetAddress, 0);

	}

	public Socket createSocket(final String host, final int port, final InetAddress localAddress, final int localPort, final HttpConnectionParams params) throws IOException, UnknownHostException, SocketTimeoutException {
		InetSocketAddress remoteInetAddress = new InetSocketAddress(host, port);
		InetSocketAddress localInetAddress = new InetSocketAddress(localAddress, localPort);
		return createSocket(remoteInetAddress, localInetAddress, params);
	}

	private Socket createSocket(final InetSocketAddress remoteInetAddress, final InetSocketAddress localInetAddress, final HttpConnectionParams params) throws IOException, UnknownHostException, SocketTimeoutException {
		if (params == null) {
			throw new IllegalArgumentException("Parameters may not be null"); //$NON-NLS-1$
		}
		int timeout = params.getConnectionTimeout();
		return createSocket(remoteInetAddress, localInetAddress, timeout);
	}

	protected Socket createSocket() throws IOException {
		return unconnectedFactory.createSocket();
	}

	private Socket createSocket(final InetSocketAddress remoteInetAddress, final InetSocketAddress localInetAddress, int timeout) throws IOException {
		final Socket socket = unconnectedFactory.createSocket();
		socketConnectCallback.onSocketCreated(socket);
		try {
			socket.bind(localInetAddress);
			socket.connect(remoteInetAddress, timeout);
		} catch (IOException e) {
			socketConnectCallback.onSocketConnectionFailed(socket, e);
			throw e;
		}
		socketConnectCallback.onSocketConnected(socket);
		return socket;
	}

	public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
		InetSocketAddress remoteInetAddress = host != null ? new InetSocketAddress(host, port) : new InetSocketAddress(InetAddress.getByName(null), port);
		InetSocketAddress localInetAddress = new InetSocketAddress(0);
		return createSocket(remoteInetAddress, localInetAddress, 0);
	}

	public boolean equals(Object obj) {
		return ((obj != null) && obj.getClass().equals(ECFHttpClientProtocolSocketFactory.class));
	}

	public int hashCode() {
		return ECFHttpClientProtocolSocketFactory.class.hashCode();
	}
}
