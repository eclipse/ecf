package org.eclipse.ecf.provider.filetransfer.events.socket;

import java.net.Socket;
import org.eclipse.ecf.filetransfer.events.socket.ISocketConnectedEvent;
import org.eclipse.ecf.filetransfer.events.socket.ISocketEventSource;

public class SocketConnectedEvent extends AbstractSocketEvent implements ISocketConnectedEvent {

	public SocketConnectedEvent(ISocketEventSource source, Socket factorySocket, Socket wrappedSocket) {
		super(source, factorySocket, wrappedSocket);
	}

	protected String getEventName() {
		return "SocketConnectedEvent"; //$NON-NLS-1$
	}

	public void setSocket(Socket socket) {
		super.setSocket(socket);
	}

}
