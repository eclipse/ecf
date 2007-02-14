/*******************************************************************************
 * Copyright (c) 2004, 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.generic;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;

import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.provider.ECFProviderDebugOptions;
import org.eclipse.ecf.internal.provider.Messages;
import org.eclipse.ecf.internal.provider.ProviderPlugin;
import org.eclipse.ecf.provider.comm.tcp.Client;
import org.eclipse.ecf.provider.comm.tcp.ConnectRequestMessage;
import org.eclipse.ecf.provider.comm.tcp.ConnectResultMessage;
import org.eclipse.ecf.provider.comm.tcp.ISocketAcceptHandler;
import org.eclipse.ecf.provider.comm.tcp.Server;

public class TCPServerSOContainerGroup extends SOContainerGroup implements
		ISocketAcceptHandler {

	public static final int DEFAULT_SOCKET_KEEPALIVE = 30000;
	public static final String INVALID_CONNECT = Messages.TCPServerSOContainerGroup_Invalid_Connect_Request;
	public static final String DEFAULT_GROUP_NAME = TCPServerSOContainerGroup.class
			.getName();
	protected int port;
	Server listener;
	boolean isOnTheAir = false;
	ThreadGroup threadGroup;
	int socketKeepAlive = DEFAULT_SOCKET_KEEPALIVE;

	public TCPServerSOContainerGroup(String name, ThreadGroup group, int port,
			int socketKeepAlive) {
		super(name);
		threadGroup = group;
		this.port = port;
		this.socketKeepAlive = socketKeepAlive;
	}

	public TCPServerSOContainerGroup(String name, ThreadGroup group, int port) {
		super(name);
		threadGroup = group;
		this.port = port;
	}

	public TCPServerSOContainerGroup(String name, int port) {
		this(name, null, port);
	}

	public TCPServerSOContainerGroup(int port) {
		this(DEFAULT_GROUP_NAME, null, port);
	}

	protected void debug(String msg) {
		Trace.trace(ProviderPlugin.getDefault(), ECFProviderDebugOptions.DEBUG,
				msg);
	}

	protected void traceStack(String msg, Throwable e) {
		Trace.catching(ProviderPlugin.getDefault(),
				ECFProviderDebugOptions.EXCEPTIONS_CATCHING,
				TCPServerSOContainerGroup.class, msg, e);
	}

	public synchronized void putOnTheAir() throws IOException {
		debug("group at port " + port + " on the air"); //$NON-NLS-1$ //$NON-NLS-2$
		listener = new Server(threadGroup, port, this);
		port = listener.getLocalPort();
		isOnTheAir = true;
	}

	public synchronized boolean isOnTheAir() {
		return isOnTheAir;
	}

	private void setSocketOptions(Socket aSocket) throws SocketException {
		aSocket.setTcpNoDelay(true);
		if (socketKeepAlive > 0) {
			aSocket.setKeepAlive(true);
			aSocket.setSoTimeout(socketKeepAlive);
		}
	}

	public void handleAccept(Socket aSocket) throws Exception {
		// Set socket options
		setSocketOptions(aSocket);
		ObjectOutputStream oStream = new ObjectOutputStream(aSocket
				.getOutputStream());
		oStream.flush();
		ObjectInputStream iStream = new ObjectInputStream(aSocket
				.getInputStream());
		ConnectRequestMessage req = (ConnectRequestMessage) iStream
				.readObject();
		debug("serverrecv:" + req); //$NON-NLS-1$
		if (req == null)
			throw new InvalidObjectException(INVALID_CONNECT
					+ Messages.TCPServerSOContainerGroup_Exception_Connect_Request_Null);
		URI uri = req.getTarget();
		if (uri == null)
			throw new InvalidObjectException(INVALID_CONNECT
					+ Messages.TCPServerSOContainerGroup_Target_Null);
		String path = uri.getPath();
		if (path == null)
			throw new InvalidObjectException(INVALID_CONNECT
					+ Messages.TCPServerSOContainerGroup_Target_Path_Null);
		TCPServerSOContainer srs = (TCPServerSOContainer) get(path);
		if (srs == null)
			throw new InvalidObjectException(Messages.TCPServerSOContainerGroup_Container_For_Target + path
					+ Messages.TCPServerSOContainerGroup_Not_Found);
		debug("found container:" + srs.getID().getName() + " for target " + uri); //$NON-NLS-1$ //$NON-NLS-2$
		// Create our local messaging interface
		Client newClient = new Client(aSocket, iStream, oStream, srs
				.getReceiver(), srs.keepAlive);
		// No other threads can access messaging interface until space has
		// accepted/rejected
		// connect request
		synchronized (newClient) {
			// Call checkConnect
			Serializable resp = srs.handleConnectRequest(aSocket, path, req
					.getData(), newClient);
			// Create connect response wrapper and send it back
			oStream.writeObject(new ConnectResultMessage(resp));
			oStream.flush();
		}
	}

	public synchronized void takeOffTheAir() {
		if (listener != null) {
			debug("Taking " + getName() + " off the air."); //$NON-NLS-1$ //$NON-NLS-2$
			try {
				listener.close();
			} catch (IOException e) {
				traceStack("Exception in closeListener", e); //$NON-NLS-1$
			}
			listener = null;
		}
		isOnTheAir = false;
	}

	public int getPort() {
		return port;
	}

	public String toString() {
		return super.toString() + ";port:" + port; //$NON-NLS-1$
	}
}