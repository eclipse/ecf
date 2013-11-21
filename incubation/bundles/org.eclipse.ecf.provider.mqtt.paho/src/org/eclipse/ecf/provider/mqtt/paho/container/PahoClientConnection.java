package org.eclipse.ecf.provider.mqtt.paho.container;

import java.io.IOException;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.provider.comm.ISynchAsynchEventHandler;
import org.eclipse.ecf.provider.comm.tcp.ConnectResultMessage;
import org.eclipse.ecf.provider.mqtt.paho.identity.PahoID;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

public class PahoClientConnection extends AbstractPahoConnection {

	public PahoClientConnection(PahoID clientID,
			ISynchAsynchEventHandler handler) {
		super(clientID, handler);
	}

	public synchronized Object connect(ID targetID, Object data, int timeout)
			throws ECFException {
		if (targetID == null)
			throw new ECFException("targetID cannot be null");
		if (!(targetID instanceof PahoID))
			throw new ECFException("targetID must be in PahoID namespace");
		PahoID pahoTargetID = (PahoID) targetID;

		MqttConnectOptions connectOpts = createConnectionOptions(targetID,
				data, timeout);

		connectAndSubscribe(pahoTargetID, connectOpts);
		ConnectResultMessage response;
		try {
			response = (ConnectResultMessage) sendSynch(pahoTargetID,
					new ConnectRequestMessage((PahoID) getLocalID(),
							pahoTargetID, data).serialize());
		} catch (IOException e) {
			throw new ECFException("Could not connect to target=" + targetID, e);
		}
		if (response == null)
			throw new ECFException("Received null response from group manager");

		return response.getData();
	}

	@Override
	protected void handleSynchRequest(SyncRequestMessage pahoMessage) {
		// TODO Auto-generated method stub
		
	}

}
