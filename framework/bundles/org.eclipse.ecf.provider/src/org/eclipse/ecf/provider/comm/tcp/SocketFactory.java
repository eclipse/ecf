/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
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

package org.eclipse.ecf.provider.comm.tcp;

import java.io.IOException;
import java.net.*;

public class SocketFactory implements IClientSocketFactory, IServerSocketFactory {
	protected static SocketFactory defaultFactory;
	protected static SocketFactory factory = null;

	public Socket createSocket(String name, int port, int timeout) throws IOException {
		if (factory != null) {
			return factory.createSocket(name, port, timeout);
		}
		Socket s = new Socket();
		s.connect(new InetSocketAddress(name, port), timeout);
		return s;
	}

	public ServerSocket createServerSocket(int port, int backlog) throws IOException {
		if (factory != null) {
			return factory.createServerSocket(port, backlog);
		}
		return new ServerSocket(port, backlog);
	}

	/**
	 * @param port port
	 * @param backlog backlog
	 * @param bindAddress bindAddress
	 * @return ServerSocket server socket created
	 * @throws IOException if server socket cannot be created
	 * @since 4.4
	 */
	public ServerSocket createServerSocket(int port, int backlog, InetAddress bindAddress) throws IOException {
		if (factory != null) {
			return factory.createServerSocket(port, backlog, bindAddress);
		}
		return new ServerSocket(port, backlog, bindAddress);
	}

	public static synchronized SocketFactory getSocketFactory() {
		return factory;
	}

	public synchronized static SocketFactory getDefaultSocketFactory() {
		if (defaultFactory == null) {
			defaultFactory = new SocketFactory();
		}
		return defaultFactory;
	}

	public synchronized static void setSocketFactory(SocketFactory fact) {
		if (!fact.equals(defaultFactory)) {
			factory = fact;
		}
	}
}