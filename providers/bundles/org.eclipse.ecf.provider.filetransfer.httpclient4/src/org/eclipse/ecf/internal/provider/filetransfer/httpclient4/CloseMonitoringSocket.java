/****************************************************************************
 * Copyright (c) 2009 IBM, and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Thomas Joiner - extracted implementation from the Socket factories
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.internal.provider.filetransfer.httpclient4;

import java.io.IOException;
import java.net.Socket;
import javax.net.ssl.SSLSocket;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.filetransfer.events.socket.ISocketEvent;
import org.eclipse.ecf.filetransfer.events.socket.ISocketEventSource;
import org.eclipse.ecf.filetransfer.events.socket.ISocketListener;
import org.eclipse.ecf.provider.filetransfer.events.socket.AbstractSocketWrapper;
import org.eclipse.ecf.provider.filetransfer.events.socket.SocketClosedEvent;

class CloseMonitoringSocket extends AbstractSocketWrapper {

	private boolean closed = false;
	private final ISocketListener spyListener;
	private final ISocketEventSource source;
	private Socket wrappedSocket;

	public CloseMonitoringSocket(Socket toWrap, ISocketListener spyListener, ISocketEventSource source) {
		super(toWrap);
		this.spyListener = spyListener;
		this.source = source;
	}

	public synchronized void close() throws IOException {
		if (!closed) {
			closed = true;

			try {
				Trace.trace(Activator.PLUGIN_ID, "closing socket " + this.toString()); //$NON-NLS-1$
				super.close();
			} finally {
				fireEvent(new SocketClosedEvent(source, getSocket(), (wrappedSocket != null ? wrappedSocket : this)));
			}
		}
	}

	private void fireEvent(ISocketEvent event) {
		if (spyListener != null) {
			spyListener.handleSocketEvent(event);
		}
		event.getSource().fireEvent(event);
	}

	public boolean isSecure() {
		return getSocket() instanceof SSLSocket;
	}

	Socket getWrappedSocket() {
		return wrappedSocket;
	}

	void setWrappedSocket(Socket wrappedSocket) {
		this.wrappedSocket = wrappedSocket;
	}

}