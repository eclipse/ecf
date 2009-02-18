package org.eclipse.ecf.provider.filetransfer.events.socket;

import java.net.Socket;
import org.eclipse.ecf.filetransfer.events.socket.ISocketClosedEvent;
import org.eclipse.ecf.filetransfer.events.socket.ISocketEventSource;

public class SocketClosedEvent extends AbstractSocketEvent implements ISocketClosedEvent {

	public SocketClosedEvent(ISocketEventSource source, Socket factorySocket, Socket wrappedSocket) {
		super(source, factorySocket, wrappedSocket);
	}

	protected String getEventName() {
		return "SocketClosedEvent"; //$NON-NLS-1$
	}

}
