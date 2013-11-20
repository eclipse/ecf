package org.eclipse.ecf.provider.mqtt.paho.container;

import java.io.IOException;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
import org.eclipse.ecf.provider.mqtt.paho.identity.PahoID;

public class PahoServerConnection extends AbstractPahoConnection implements ISynchAsynchConnection {

	public PahoServerConnection(PahoID clientID) {
		super(clientID);
	}

	public synchronized Object connect(ID targetID, Object data, int timeout)
			throws ECFException {
		throw new ECFException("Server cannot be connected");
	}

	public void start() {
		// TODO Auto-generated method stub

	}

	public void stop() {
		// TODO Auto-generated method stub

	}

	public boolean isStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	public Object sendSynch(ID receiver, byte[] data) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
