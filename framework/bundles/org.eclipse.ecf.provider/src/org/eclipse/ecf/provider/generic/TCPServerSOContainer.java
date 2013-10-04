/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.provider.generic;

import java.io.IOException;
import java.io.Serializable;
import java.net.*;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainerConfig;
import org.eclipse.ecf.provider.comm.IConnectRequestHandler;
import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
import org.eclipse.ecf.provider.comm.tcp.Server;

public class TCPServerSOContainer extends ServerSOContainer implements IConnectRequestHandler {
	public static final String DEFAULT_PROTOCOL = "ecftcp"; //$NON-NLS-1$ 

	public static final int DEFAULT_PORT = Integer.parseInt(System.getProperty("org.eclipse.ecf.provider.generic.port", "3282")); //$NON-NLS-1$ //$NON-NLS-2$;

	public static final int DEFAULT_KEEPALIVE = Integer.parseInt(System.getProperty("org.eclipse.ecf.provider.generic.keepalive", "30000")); //$NON-NLS-1$ //$NON-NLS-2$;

	public static final String DEFAULT_NAME = System.getProperty("org.eclipse.ecf.provider.generic.name", "/server"); //$NON-NLS-1$ //$NON-NLS-2$"

	public static String DEFAULT_HOST = System.getProperty("org.eclipse.ecf.provider.generic.host", "localhost"); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * @since 4.2
	 */
	public static final boolean DEFAULT_FALLBACK_PORT = Boolean.valueOf(System.getProperty("org.eclipse.ecf.provider.generic.port.fallback", "true")).booleanValue(); //$NON-NLS-1$//$NON-NLS-2$

	static {
		final Boolean useHostname = Boolean.valueOf(System.getProperty("org.eclipse.ecf.provider.generic.host.useHostName", "true")); //$NON-NLS-1$ //$NON-NLS-2$
		if (useHostname.booleanValue()) {
			try {
				DEFAULT_HOST = InetAddress.getLocalHost().getCanonicalHostName();
			} catch (UnknownHostException e) {
				DEFAULT_HOST = "localhost"; //$NON-NLS-1$
			}
		}
	}

	// Keep alive value
	protected int keepAlive;

	protected TCPServerSOContainerGroup group;

	protected boolean isSingle = false;

	protected int getKeepAlive() {
		return keepAlive;
	}

	public static String getServerURL(String host, String name) {
		return DEFAULT_PROTOCOL + "://" + host + ":" + DEFAULT_PORT + name; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static String getDefaultServerURL() {
		return getServerURL("localhost", DEFAULT_NAME); //$NON-NLS-1$
	}

	/**
	 * @since 4.4
	 */
	public TCPServerSOContainer(ISharedObjectContainerConfig config, int port, InetAddress bindAddress, String path, int keepAlive) throws IOException {
		super(config);
		isSingle = true;
		if (path == null)
			throw new NullPointerException("path cannot be null"); //$NON-NLS-1$
		this.group = new TCPServerSOContainerGroup(TCPServerSOContainerGroup.DEFAULT_GROUP_NAME, null, Server.DEFAULT_BACKLOG, port, bindAddress);
		this.group.add(path, this);
		this.group.putOnTheAir();
	}

	/**
	 * @since 4.4
	 */
	public TCPServerSOContainer(ISharedObjectContainerConfig config, InetAddress bindAddress, int keepAlive) throws IOException, URISyntaxException {
		super(config);
		isSingle = true;
		URI actualURI = new URI(getID().getName());
		int port = actualURI.getPort();
		String path = actualURI.getPath();
		if (path == null)
			throw new NullPointerException("path cannot be null"); //$NON-NLS-1$
		this.group = new TCPServerSOContainerGroup(TCPServerSOContainerGroup.DEFAULT_GROUP_NAME, null, port, Server.DEFAULT_BACKLOG, bindAddress);
		this.group.add(path, this);
		this.group.putOnTheAir();
	}

	public TCPServerSOContainer(ISharedObjectContainerConfig config, TCPServerSOContainerGroup grp, int keepAlive) throws IOException, URISyntaxException {
		super(config);
		this.keepAlive = keepAlive;
		// Make sure URI syntax is followed.
		URI actualURI = new URI(getID().getName());
		int urlPort = actualURI.getPort();
		String path = actualURI.getPath();
		if (grp == null) {
			isSingle = true;
			this.group = new TCPServerSOContainerGroup(urlPort);
		} else
			this.group = grp;
		group.add(path, this);
		if (grp == null)
			this.group.putOnTheAir();
	}

	public TCPServerSOContainer(ISharedObjectContainerConfig config, TCPServerSOContainerGroup listener, String path, int keepAlive) {
		super(config);
		initialize(listener, path, keepAlive);
	}

	protected void initialize(TCPServerSOContainerGroup listener, String path, int ka) {
		this.keepAlive = ka;
		this.group = listener;
		this.group.add(path, this);
	}

	public void dispose() {
		URI aURI = null;
		try {
			aURI = new URI(getID().getName());
		} catch (Exception e) {
			// Should never happen
		}
		group.remove(aURI.getPath());
		if (isSingle)
			group.takeOffTheAir();
		super.dispose();
	}

	public TCPServerSOContainer(ISharedObjectContainerConfig config) throws IOException, URISyntaxException {
		this(config, (TCPServerSOContainerGroup) null, DEFAULT_KEEPALIVE);
	}

	public TCPServerSOContainer(ISharedObjectContainerConfig config, int keepAlive) throws IOException, URISyntaxException {
		this(config, (TCPServerSOContainerGroup) null, keepAlive);
	}

	public Serializable handleConnectRequest(Socket socket, String target, Serializable data, ISynchAsynchConnection conn) {
		return acceptNewClient(socket, target, data, conn);
	}

	protected Serializable getConnectDataFromInput(Serializable input) throws Exception {
		return input;
	}

}