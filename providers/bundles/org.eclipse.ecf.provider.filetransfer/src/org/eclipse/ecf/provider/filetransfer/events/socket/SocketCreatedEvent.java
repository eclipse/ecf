/****************************************************************************
 * Copyright (c) 2009 IBM, and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   IBM Corporation - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.provider.filetransfer.events.socket;

import java.net.Socket;
import org.eclipse.ecf.filetransfer.events.socket.ISocketCreatedEvent;
import org.eclipse.ecf.filetransfer.events.socket.ISocketEventSource;

public class SocketCreatedEvent extends AbstractSocketEvent implements ISocketCreatedEvent {

	public SocketCreatedEvent(ISocketEventSource source, Socket socket) {
		super(source, socket, socket);
	}

	protected String getEventName() {
		return "SocketCreatedEvent"; //$NON-NLS-1$
	}

}
