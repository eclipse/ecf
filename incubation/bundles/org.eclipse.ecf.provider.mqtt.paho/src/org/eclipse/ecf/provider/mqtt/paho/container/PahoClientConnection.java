package org.eclipse.ecf.provider.mqtt.paho.container;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
import org.eclipse.ecf.provider.mqtt.paho.identity.PahoID;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class PahoClientConnection extends AbstractPahoConnection implements
		ISynchAsynchConnection {

	public PahoClientConnection(PahoID clientID) {
		super(clientID);
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

		try {
			// publish to topic
			this.client.publish(pahoTargetID.getTopic(), new MqttMessage());
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// wait for response
		// ConnectResponseMessage crm =
		// AbstractMessage.createFromByteArray(bytes);
		// TODO Auto-generated method stub
		return null;
	}

	public void disconnect() {
		// TODO Auto-generated method stub

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

	@Override
	protected void handleMessageArrived(String topic2, MqttMessage message) {
		// TODO Auto-generated method stub

	}

}
