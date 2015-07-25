/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thomas Joiner - HttpClient 4 implementation
 *******************************************************************************/
package org.eclipse.ecf.internal.provider.filetransfer.httpclient4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSchemeSocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.filetransfer.events.socket.ISocketEvent;
import org.eclipse.ecf.filetransfer.events.socket.ISocketEventSource;
import org.eclipse.ecf.filetransfer.events.socket.ISocketListener;
import org.eclipse.ecf.provider.filetransfer.events.socket.SocketClosedEvent;
import org.eclipse.ecf.provider.filetransfer.events.socket.SocketConnectedEvent;
import org.eclipse.ecf.provider.filetransfer.events.socket.SocketCreatedEvent;

public final class ECFHttpClientSecureProtocolSocketFactory implements LayeredSchemeSocketFactory {

	private final SSLSocketFactory sslSocketFactory;
	private final ISocketEventSource source;
	private final ISocketListener socketConnectListener;

	public ECFHttpClientSecureProtocolSocketFactory(final SSLSocketFactory sslSocketFactory, ISocketEventSource source, ISocketListener socketConnectListener) {
		this.sslSocketFactory = sslSocketFactory;
		this.source = source;
		this.socketConnectListener = socketConnectListener;
	}

	public Socket createSocket(final HttpParams params) {
		Trace.entering(Activator.PLUGIN_ID, DebugOptions.METHODS_ENTERING, ECFHttpClientSecureProtocolSocketFactory.class, "createSocket"); //$NON-NLS-1$

		Socket socket = new Socket();
		fireEvent(socketConnectListener, new SocketCreatedEvent(source, socket));

		Trace.exiting(Activator.PLUGIN_ID, DebugOptions.METHODS_EXITING, ECFHttpClientSecureProtocolSocketFactory.class, "socketCreated " + socket); //$NON-NLS-1$
		return socket;
	}

	public Socket connectSocket(final Socket socket, final InetSocketAddress remoteAddress, final InetSocketAddress localAddress, final HttpParams params) throws IOException, UnknownHostException, ConnectTimeoutException {
		if (remoteAddress == null) {
			throw new IllegalArgumentException("Remote address must not be null"); //$NON-NLS-1$
		}
		if (params == null) {
			throw new IllegalArgumentException("HTTP parameters must not be null"); //$NON-NLS-1$
		}

		if (socket == null) {
			SSLSocket sslSocket = (SSLSocket) this.sslSocketFactory.createSocket();

			performConnection(sslSocket, remoteAddress, localAddress, params);

			return wrapSocket(sslSocket);
		} else if (socket instanceof SSLSocket) {
			performConnection(socket, remoteAddress, localAddress, params);

			return wrapSocket(socket);
		}

		// If we were given a unconnected socket, we need to connect it first
		if (!socket.isConnected()) {
			performConnection(socket, remoteAddress, localAddress, params);
		}

		Socket layeredSocket = this.sslSocketFactory.createSocket(socket, remoteAddress.getHostName(), remoteAddress.getPort(), true);

		return wrapSocket(layeredSocket);
	}

	private void performConnection(final Socket socket, final InetSocketAddress remoteAddress, final InetSocketAddress localAddress, final HttpParams params) throws SocketException, IOException {
		try {
			socket.setReuseAddress(HttpConnectionParams.getSoReuseaddr(params));
			if (localAddress != null) {
				// only explicitly bind if a local address is actually provided (bug 444377)
				socket.bind(localAddress);
			}

			int connectionTimeout = HttpConnectionParams.getConnectionTimeout(params);
			int socketTimeout = HttpConnectionParams.getSoTimeout(params);

			socket.connect(remoteAddress, connectionTimeout);
			socket.setSoTimeout(socketTimeout);
		} catch (SocketException e) {
			Trace.catching(Activator.PLUGIN_ID, DebugOptions.EXCEPTIONS_CATCHING, ECFHttpClientSecureProtocolSocketFactory.class, "performConnection", e); //$NON-NLS-1$
			fireEvent(this.socketConnectListener, new SocketClosedEvent(source, socket, socket));
			Trace.throwing(Activator.PLUGIN_ID, DebugOptions.EXCEPTIONS_THROWING, ECFHttpClientSecureProtocolSocketFactory.class, "performConnection", e); //$NON-NLS-1$
			throw e;
		} catch (IOException e) {
			Trace.catching(Activator.PLUGIN_ID, DebugOptions.EXCEPTIONS_CATCHING, ECFHttpClientSecureProtocolSocketFactory.class, "performConnection", e); //$NON-NLS-1$
			fireEvent(this.socketConnectListener, new SocketClosedEvent(source, socket, socket));
			Trace.throwing(Activator.PLUGIN_ID, DebugOptions.EXCEPTIONS_THROWING, ECFHttpClientSecureProtocolSocketFactory.class, "performConnection", e); //$NON-NLS-1$
			throw e;
		}
	}

	public boolean isSecure(final Socket sock) throws IllegalArgumentException {
		if (sock == null) {
			throw new IllegalArgumentException("Socket must not be null"); //$NON-NLS-1$
		}

		if (sock instanceof CloseMonitoringSocket) {
			return ((CloseMonitoringSocket) sock).isSecure();
		}

		if (!(sock instanceof SSLSocket)) {
			throw new IllegalArgumentException("Socket not created by this factory"); //$NON-NLS-1$
		}

		if (sock.isClosed()) {
			throw new IllegalArgumentException("Socket is closed"); //$NON-NLS-1$
		}

		return true;
	}

	public Socket createLayeredSocket(final Socket socket, final String host, final int port, final boolean autoClose) throws IOException {
		Trace.entering(Activator.PLUGIN_ID, DebugOptions.METHODS_ENTERING, ECFHttpClientSecureProtocolSocketFactory.class, "createLayeredSocket " + host + ":" + port + ",socket=" + socket); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		SSLSocket sslSocket = null;
		try {
			Trace.trace(Activator.PLUGIN_ID, "connectingLayeredSocket(original=" + socket + ",dest=" + host + ":" + port + ")"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
			sslSocket = (SSLSocket) this.sslSocketFactory.createSocket(socket, host, port, autoClose);
			Trace.trace(Activator.PLUGIN_ID, "connected"); //$NON-NLS-1$
		} catch (IOException e) {
			Trace.catching(Activator.PLUGIN_ID, DebugOptions.EXCEPTIONS_CATCHING, ECFHttpClientSecureProtocolSocketFactory.class, "createLayeredSocket", e); //$NON-NLS-1$
			fireEvent(this.socketConnectListener, new SocketClosedEvent(source, sslSocket, sslSocket));
			Trace.throwing(Activator.PLUGIN_ID, DebugOptions.EXCEPTIONS_THROWING, ECFHttpClientSecureProtocolSocketFactory.class, "createSocket", e); //$NON-NLS-1$
			throw e;
		}

		return wrapSocket(sslSocket);
	}

	private Socket wrapSocket(Socket toWrap) {
		CloseMonitoringSocket wrappedSocket = new CloseMonitoringSocket(toWrap, socketConnectListener, source);

		SocketConnectedEvent connectedEvent = new SocketConnectedEvent(source, toWrap, wrappedSocket);
		fireEvent(socketConnectListener, connectedEvent);

		// Change the wrapped socket if one of the receivers of the SocketConnectedEvent changed it
		Socket connectedEventSocket = connectedEvent.getSocket();
		if (connectedEventSocket != wrappedSocket) {
			wrappedSocket.setWrappedSocket(connectedEventSocket);
			return connectedEventSocket;
		}

		return wrappedSocket;
	}

	static void fireEvent(final ISocketListener spyListener, ISocketEvent event) {
		if (spyListener != null) {
			spyListener.handleSocketEvent(event);
		}
		event.getSource().fireEvent(event);
	}

	public boolean equals(Object obj) {
		return ((obj != null) && obj.getClass().equals(ECFHttpClientSecureProtocolSocketFactory.class));
	}

	public int hashCode() {
		return ECFHttpClientSecureProtocolSocketFactory.class.hashCode();
	}

}
