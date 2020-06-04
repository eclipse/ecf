/****************************************************************************
 * Copyright (c) 2006, 2008 Remy Suen, Composent Inc., and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.protocol.bittorrent.internal.net;

import java.nio.channels.SocketChannel;

class ConnectionInfo {

	private SocketChannel channel;

	private String ip;

	private int port;

	ConnectionInfo(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	ConnectionInfo(SocketChannel channel) {
		this.channel = channel;
	}

	String getIP() {
		return ip;
	}

	int getPort() {
		return port;
	}

	SocketChannel getChannel() {
		return channel;
	}

	boolean isChannel() {
		return channel != null;
	}

}
