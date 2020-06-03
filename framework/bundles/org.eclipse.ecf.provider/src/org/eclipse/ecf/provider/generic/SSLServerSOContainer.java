/****************************************************************************
 * Copyright (c) 2012 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.provider.generic;

import java.io.IOException;
import java.io.Serializable;
import java.net.*;
import javax.net.ssl.SSLServerSocket;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainerConfig;
import org.eclipse.ecf.provider.comm.IConnectRequestHandler;
import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
import org.eclipse.ecf.provider.comm.tcp.Server;

/**
 * @since 4.3
 */
public class SSLServerSOContainer extends ServerSOContainer implements IConnectRequestHandler {
	public static final String DEFAULT_PROTOCOL = "ecfssl"; //$NON-NLS-1$

	public static final int DEFAULT_PORT = Integer.parseInt(System.getProperty("org.eclipse.ecf.provider.generic.secure.port", "4282")); //$NON-NLS-1$ //$NON-NLS-2$;

	public static final int DEFAULT_KEEPALIVE = Integer.parseInt(System.getProperty("org.eclipse.ecf.provider.generic.secure.keepalive", "30000")); //$NON-NLS-1$ //$NON-NLS-2$;

	public static final String DEFAULT_NAME = System.getProperty("org.eclipse.ecf.provider.generic.secure.name", "/secureserver"); //$NON-NLS-1$ //$NON-NLS-2$"

	public static String DEFAULT_HOST = System.getProperty("org.eclipse.ecf.provider.generic.secure.host", "localhost"); //$NON-NLS-1$ //$NON-NLS-2$

	public static final boolean DEFAULT_FALLBACK_PORT = Boolean.valueOf(System.getProperty("org.eclipse.ecf.provider.generic.secure.port.fallback", "true")).booleanValue(); //$NON-NLS-1$//$NON-NLS-2$

	static {
		final Boolean useHostname = Boolean.valueOf(System.getProperty("org.eclipse.ecf.provider.generic.secure.host.useHostName", "true")); //$NON-NLS-1$ //$NON-NLS-2$
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

	protected SSLServerSOContainerGroup group;

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
	 * @param config config
	 * @param port port
	 * @param bindAddress bind address
	 * @param path path
	 * @param keepAlive keep alive
	 * @throws IOException if some problem 
	 * @since 4.4
	 */
	public SSLServerSOContainer(ISharedObjectContainerConfig config, int port, InetAddress bindAddress, String path, int keepAlive) throws IOException {
		super(config);
		isSingle = true;
		this.keepAlive = keepAlive;
		if (path == null)
			throw new NullPointerException("path cannot be null"); //$NON-NLS-1$
		this.group = new SSLServerSOContainerGroup(SSLServerSOContainerGroup.DEFAULT_GROUP_NAME, null, Server.DEFAULT_BACKLOG, port, bindAddress);
		this.group.add(path, this);
		this.group.putOnTheAir();
	}

	/**
	 * @param config config
	 * @param sslServerSocket socket
	 * @param keepAlive keep alive
	 * @throws IOException if some problem 
	 * @throws URISyntaxException if some problem
	 * @since 4.6
	 */
	public SSLServerSOContainer(ISharedObjectContainerConfig config, SSLServerSocket sslServerSocket, int keepAlive) throws IOException, URISyntaxException {
		super(config);
		this.keepAlive = keepAlive;
		URI actualURI = new URI(getID().getName());
		String path = actualURI.getPath();
		if (path == null)
			throw new NullPointerException("path cannot be null"); //$NON-NLS-1$
		this.group = new SSLServerSOContainerGroup(SSLServerSOContainerGroup.DEFAULT_GROUP_NAME, null, sslServerSocket);
		this.group.add(path, this);
		this.group.putOnTheAir();
	}

	/**
	 * @param config config
	 * @param bindAddress bind address
	 * @param keepAlive keep alive
	 * @throws IOException if some problem 
	 * @throws URISyntaxException if some problem with uri syntax based upon getID().getName()
	 * @since 4.4
	 */
	public SSLServerSOContainer(ISharedObjectContainerConfig config, InetAddress bindAddress, int keepAlive) throws IOException, URISyntaxException {
		super(config);
		this.keepAlive = keepAlive;
		isSingle = true;
		URI actualURI = new URI(getID().getName());
		int port = actualURI.getPort();
		String path = actualURI.getPath();
		if (path == null)
			throw new NullPointerException("path cannot be null"); //$NON-NLS-1$
		this.group = new SSLServerSOContainerGroup(SSLServerSOContainerGroup.DEFAULT_GROUP_NAME, null, port, Server.DEFAULT_BACKLOG, bindAddress);
		this.group.add(path, this);
		this.group.putOnTheAir();
	}

	public SSLServerSOContainer(ISharedObjectContainerConfig config, SSLServerSOContainerGroup grp, int keepAlive) throws IOException, URISyntaxException {
		super(config);
		this.keepAlive = keepAlive;
		// Make sure URI syntax is followed.
		URI actualURI = new URI(getID().getName());
		int urlPort = actualURI.getPort();
		String path = actualURI.getPath();
		if (grp == null) {
			isSingle = true;
			this.group = new SSLServerSOContainerGroup(urlPort);
		} else
			this.group = grp;
		group.add(path, this);
		if (grp == null)
			this.group.putOnTheAir();
	}

	public SSLServerSOContainer(ISharedObjectContainerConfig config, SSLServerSOContainerGroup listener, String path, int keepAlive) {
		super(config);
		initialize(listener, path, keepAlive);
	}

	protected void initialize(SSLServerSOContainerGroup listener, String path, int ka) {
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

	public SSLServerSOContainer(ISharedObjectContainerConfig config) throws IOException, URISyntaxException {
		this(config, (SSLServerSOContainerGroup) null, DEFAULT_KEEPALIVE);
	}

	public SSLServerSOContainer(ISharedObjectContainerConfig config, int keepAlive) throws IOException, URISyntaxException {
		this(config, (SSLServerSOContainerGroup) null, keepAlive);
	}

	public Serializable handleConnectRequest(Socket socket, String target, Serializable data, ISynchAsynchConnection conn) {
		return acceptNewClient(socket, target, data, conn);
	}

	protected Serializable getConnectDataFromInput(Serializable input) throws Exception {
		return input;
	}

}