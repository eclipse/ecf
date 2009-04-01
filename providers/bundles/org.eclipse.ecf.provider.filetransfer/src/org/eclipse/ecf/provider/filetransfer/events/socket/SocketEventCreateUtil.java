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
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.filetransfer.events.socket.ISocketEvent;
import org.eclipse.ecf.filetransfer.events.socket.ISocketEventSource;
import org.eclipse.ecf.filetransfer.events.socket.ISocketListener;
import org.eclipse.ecf.filetransfer.events.socketfactory.INonconnectedSocketFactory;
import org.eclipse.ecf.internal.filetransfer.Activator;
import org.eclipse.ecf.internal.provider.filetransfer.DebugOptions;

public class SocketEventCreateUtil {

	static void fireEvent(final ISocketListener spyListener, ISocketEvent event) {
		if (spyListener != null) {
			spyListener.handleSocketEvent(event);
		}
		event.getSource().fireEvent(event);
	}

	public static Socket createSocket(final ISocketListener spyListener, final ISocketEventSource socketEventSource, final INonconnectedSocketFactory unconnectedFactory, final InetSocketAddress remoteInetAddress, final InetSocketAddress localInetAddress, int timeout) throws IOException {
		Trace.entering(Activator.PLUGIN_ID, DebugOptions.METHODS_ENTERING, SocketEventCreateUtil.class, "createSocket " + remoteInetAddress.toString() + " timeout=" + timeout); //$NON-NLS-1$ //$NON-NLS-2$

		final Socket factorySocket = unconnectedFactory.createSocket();
		fireEvent(spyListener, new SocketCreatedEvent(socketEventSource, factorySocket));
		try {
			Trace.trace(Activator.PLUGIN_ID, "bind(" + localInetAddress.toString() + ")"); //$NON-NLS-1$//$NON-NLS-2$
			factorySocket.bind(localInetAddress);
			Trace.trace(Activator.PLUGIN_ID, "connect(" + remoteInetAddress.toString() + ", " + timeout + ")"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			factorySocket.connect(remoteInetAddress, timeout);
			Trace.trace(Activator.PLUGIN_ID, "connected"); //$NON-NLS-1$
		} catch (IOException e) {
			Trace.catching(Activator.PLUGIN_ID, DebugOptions.EXCEPTIONS_CATCHING, SocketEventCreateUtil.class, "createSocket", e); //$NON-NLS-1$
			fireEvent(spyListener, new SocketClosedEvent(socketEventSource, factorySocket, factorySocket));
			Trace.throwing(Activator.PLUGIN_ID, DebugOptions.EXCEPTIONS_THROWING, SocketEventCreateUtil.class, "createSocket", e); //$NON-NLS-1$
			throw e;
		}
		final Socket[] wrap = new Socket[1];
		final Socket myWrap = new AbstractSocketWrapper(factorySocket) {
			public void close() throws IOException {
				try {
					Trace.trace(Activator.PLUGIN_ID, "closing socket " + this.toString()); //$NON-NLS-1$
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
