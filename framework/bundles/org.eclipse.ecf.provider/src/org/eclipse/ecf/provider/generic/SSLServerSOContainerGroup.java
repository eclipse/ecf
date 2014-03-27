/*******************************************************************************
 * Copyright (c) 2012 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.generic;

import java.io.*;
import java.net.*;
import javax.net.ssl.SSLServerSocketFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.provider.ECFProviderDebugOptions;
import org.eclipse.ecf.internal.provider.ProviderPlugin;
import org.eclipse.ecf.provider.comm.tcp.*;

/**
 * @since 4.3
 */
public class SSLServerSOContainerGroup extends SOContainerGroup implements ISocketAcceptHandler {

	public static final int DEFAULT_BACKLOG = 50;
	public static final String INVALID_CONNECT = "Invalid connect request."; //$NON-NLS-1$
	public static final String DEFAULT_GROUP_NAME = SSLServerSOContainerGroup.class.getName();
	private int port = 0;
	private int backlog = DEFAULT_BACKLOG;
	ServerSocket serverSocket;
	private boolean isOnTheAir = false;
	private ThreadGroup threadGroup;
	private InetAddress inetAddress;
	private Thread listenerThread;

	public SSLServerSOContainerGroup(String name, ThreadGroup group, int port, int backlog, InetAddress inetAddress) {
		super(name);
		this.threadGroup = group;
		this.port = port;
		this.backlog = backlog;
		this.inetAddress = inetAddress;
		listenerThread = setupListener();
	}

	public SSLServerSOContainerGroup(String name, ThreadGroup group, int port, int backlog) {
		this(name, group, port, backlog, null);
	}

	/**
	 * @since 4.4
	 */
	public SSLServerSOContainerGroup(String name, ThreadGroup group, int port, InetAddress bindAddress) {
		this(name, group, port, Server.DEFAULT_BACKLOG, bindAddress);
	}

	public SSLServerSOContainerGroup(String name, ThreadGroup group, int port) {
		this(name, group, port, DEFAULT_BACKLOG);
	}

	public SSLServerSOContainerGroup(String name, int port) {
		this(name, null, port);
	}

	public SSLServerSOContainerGroup(int port) {
		this(DEFAULT_GROUP_NAME, null, port);
	}

	private SSLServerSocketFactory getSSLServerSocketFactory() {
		return ProviderPlugin.getDefault().getSSLServerSocketFactory();
	}

	protected void trace(String msg) {
		Trace.trace(ProviderPlugin.PLUGIN_ID, ECFProviderDebugOptions.DEBUG, msg);
	}

	protected void traceStack(String msg, Throwable e) {
		Trace.catching(ProviderPlugin.PLUGIN_ID, ECFProviderDebugOptions.EXCEPTIONS_CATCHING, SSLServerSOContainerGroup.class, msg, e);
	}

	private ServerSocket createServerSocket() throws IOException {
		SSLServerSocketFactory socketFactory = getSSLServerSocketFactory();
		if (socketFactory == null)
			throw new IOException("Cannot get SSLServerSocketFactory to create SSLServerSocket"); //$NON-NLS-1$
		return socketFactory.createServerSocket(port, backlog, inetAddress);
	}

	public synchronized void putOnTheAir() throws IOException {
		trace("SSLServerSOContainerGroup at port " + port + " on the air"); //$NON-NLS-1$ //$NON-NLS-2$
		serverSocket = createServerSocket();
		port = serverSocket.getLocalPort();
		isOnTheAir = true;
		listenerThread.start();
	}

	public synchronized boolean isOnTheAir() {
		return isOnTheAir;
	}

	void handleSyncAccept(Socket aSocket) throws Exception {
		// Set socket options
		aSocket.setTcpNoDelay(true);
		final ObjectOutputStream oStream = new ObjectOutputStream(aSocket.getOutputStream());
		oStream.flush();
		final ObjectInputStream iStream = new ObjectInputStream(aSocket.getInputStream());
		final ConnectRequestMessage req = (ConnectRequestMessage) iStream.readObject();
		if (req == null)
			throw new InvalidObjectException(INVALID_CONNECT + " Connect request message cannot be null"); //$NON-NLS-1$
		final URI uri = req.getTarget();
		if (uri == null)
			throw new InvalidObjectException(INVALID_CONNECT + " URI connect target cannot be null"); //$NON-NLS-1$
		final String path = uri.getPath();
		if (path == null)
			throw new InvalidObjectException(INVALID_CONNECT + " Path cannot be null"); //$NON-NLS-1$
		final SSLServerSOContainer srs = (SSLServerSOContainer) get(path);
		if (srs == null)
			throw new InvalidObjectException("Container not found for path=" + path); //$NON-NLS-1$
		// Create our local messaging interface
		final Client newClient = new Client(aSocket, iStream, oStream, srs.getReceiver());
		// No other threads can access messaging interface until space has
		// accepted/rejected
		// connect request
		synchronized (newClient) {
			// Call checkConnect
			final Serializable resp = srs.handleConnectRequest(aSocket, path, req.getData(), newClient);
			// Create connect response wrapper and send it back
			oStream.writeObject(new ConnectResultMessage(resp));
			oStream.flush();
		}
	}

	public synchronized void takeOffTheAir() {
		trace("Taking " + getName() + " off the air."); //$NON-NLS-1$ //$NON-NLS-2$
		if (listenerThread != null) {
			listenerThread.interrupt();
			listenerThread = null;
		}
		if (threadGroup != null) {
			threadGroup.interrupt();
			threadGroup = null;
		}
		if (this.serverSocket != null) {
			try {
				this.serverSocket.close();
			} catch (IOException e) {
				Trace.catching("org.eclipse.ecf.provider", ECFProviderDebugOptions.CONNECTION, SSLServerSOContainerGroup.class, "takeOffTheAir", e); //$NON-NLS-1$ //$NON-NLS-2$
			}
			this.serverSocket = null;
		}
		isOnTheAir = false;
	}

	public synchronized int getPort() {
		return port;
	}

	public synchronized String toString() {
		return super.toString() + ";port:" + port; //$NON-NLS-1$
	}

	protected Thread setupListener() {
		return new Thread(threadGroup, new Runnable() {
			public void run() {
				while (true) {
					try {
						handleAccept(serverSocket.accept());
					} catch (Exception e) {
						traceStack("Exception in accept", e); //$NON-NLS-1$
						// If we get an exception on accept(), we should just
						// exit
						break;
					}
				}
				debug("SSLServerSOContaienrGroup closing listener normally."); //$NON-NLS-1$
			}
		}, "SSLServerSOContainerGroup(" + port + ")"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void handleAccept(final Socket aSocket) {
		new Thread(threadGroup, new Runnable() {
			public void run() {
				try {
					debug("accept:" + aSocket.getInetAddress()); //$NON-NLS-1$
					handleSyncAccept(aSocket);
				} catch (Exception e) {
					traceStack("Unexpected exception in handleAccept...closing", //$NON-NLS-1$
							e);
					try {
						aSocket.close();
					} catch (IOException e1) {
						ProviderPlugin.getDefault().log(new Status(IStatus.ERROR, ProviderPlugin.PLUGIN_ID, IStatus.ERROR, "accept.close", e1)); //$NON-NLS-1$
					}
				}
			}
		}).start();
	}

	protected void debug(String msg) {
		Trace.trace(ProviderPlugin.PLUGIN_ID, ECFProviderDebugOptions.CONNECTION, msg);
	}

}