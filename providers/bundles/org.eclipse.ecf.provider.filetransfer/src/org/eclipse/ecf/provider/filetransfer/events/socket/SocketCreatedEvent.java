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
