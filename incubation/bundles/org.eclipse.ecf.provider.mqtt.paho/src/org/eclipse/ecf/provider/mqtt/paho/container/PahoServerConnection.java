package org.eclipse.ecf.provider.mqtt.paho.container;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
import org.eclipse.ecf.provider.comm.ISynchAsynchEventHandler;
import org.eclipse.ecf.provider.mqtt.paho.identity.PahoID;

public class PahoServerConnection extends AbstractPahoConnection implements
		ISynchAsynchConnection {

	public PahoServerConnection(PahoID clientID,
			ISynchAsynchEventHandler handler) {
		super(clientID, handler);
	}

	public synchronized Object connect(ID targetID, Object data, int timeout)
			throws ECFException {
		throw new ECFException("Server cannot be connected");
	}

	@Override
	protected void handleSynchRequest(SyncRequestMessage pahoMessage) {
		// TODO Auto-generated method stub
		
	}

}
