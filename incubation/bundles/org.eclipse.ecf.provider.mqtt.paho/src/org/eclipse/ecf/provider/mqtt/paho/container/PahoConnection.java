package org.eclipse.ecf.provider.mqtt.paho.container;

import java.io.IOException;
import java.util.Map;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.provider.comm.IConnectionListener;
import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
import org.eclipse.ecf.provider.mqtt.paho.identity.PahoID;

public class PahoConnection implements ISynchAsynchConnection {

	public PahoConnection(PahoID targetID, Object data) {
		// TODO Auto-generated constructor stub
	}

	public void sendAsynch(ID receiver, byte[] data) throws IOException {
		// TODO Auto-generated method stub

	}

	public Object connect(ID targetID, Object data, int timeout)
			throws ECFException {
		// TODO Auto-generated method stub
		return null;
	}

	public void disconnect() {
		// TODO Auto-generated method stub

	}

	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	public ID getLocalID() {
		// TODO Auto-generated method stub
		return null;
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

	@SuppressWarnings("rawtypes")
	public Map getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addListener(IConnectionListener listener) {
		// TODO Auto-generated method stub

	}

	public void removeListener(IConnectionListener listener) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object sendSynch(ID receiver, byte[] data) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
