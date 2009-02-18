/*******************************************************************************
* Copyright (c) 2009 IBM, and others. 
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   IBM Corporation - initial API and implementation
******************************************************************************/

package org.eclipse.ecf.provider.filetransfer.events.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import org.eclipse.ecf.filetransfer.events.socket.*;
import org.eclipse.ecf.filetransfer.events.socketfactory.INonconnectedSocketFactory;

public class SocketEventCreateUtil {

	static void fireEvent(final ISocketListener spyListener, ISocketEvent event) {
		if (spyListener != null) {
			spyListener.handleSocketEvent(event);
		}
		event.getSource().fireEvent(event);
	}

	public static Socket createSocket(final ISocketListener spyListener, final ISocketEventSource socketEventSource, final INonconnectedSocketFactory unconnectedFactory, final InetSocketAddress remoteInetAddress, final InetSocketAddress localInetAddress, int timeout) throws IOException {
		final Socket factorySocket = unconnectedFactory.createSocket();
		fireEvent(spyListener, new SocketCreatedEvent(socketEventSource, factorySocket));
		try {
			factorySocket.bind(localInetAddress);
			factorySocket.connect(remoteInetAddress, timeout);
		} catch (IOException e) {
			fireEvent(spyListener, new SocketClosedEvent(socketEventSource, factorySocket, factorySocket));
			throw e;
		}
		final Socket[] wrap = new Socket[1];
		final Socket myWrap = new AbstractSocketWrapper(factorySocket) {
			public void close() throws IOException {
				try {
					super.close();
				} finally {
					fireEvent(spyListener, new SocketClosedEvent(socketEventSource, factorySocket, wrap[0]));
				}
			}
		};
		SocketConnectedEvent connectedEvent = new SocketConnectedEvent(socketEventSource, factorySocket, myWrap);
		fireEvent(spyListener, connectedEvent);
		if (connectedEvent.getSocket() == myWrap) {
			wrap[0] = myWrap;
		} else {
			wrap[0] = connectedEvent.getSocket();
		}

		return wrap[0];
	}

}
