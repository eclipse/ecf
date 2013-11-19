package org.eclipse.ecf.provider.mqtt.paho.container;

import java.io.IOException;
import java.util.Map;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.provider.comm.IConnectionListener;
import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
import org.eclipse.ecf.provider.mqtt.paho.identity.PahoID;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

public class PahoConnection implements ISynchAsynchConnection {

	private PahoID clientID;
	private MqttClient client;
	private String mqttTopic;
	
	private MqttCallback callback = new MqttCallback() {

		public void connectionLost(Throwable cause) {
			// TODO Auto-generated method stub
			
		}

		public void messageArrived(String topic, MqttMessage message)
				throws Exception {
			// TODO Auto-generated method stub
			
		}

		public void deliveryComplete(IMqttDeliveryToken token) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	public PahoConnection() {
	}

	public synchronized void sendAsynch(ID receiver, byte[] data) throws IOException {
		// TODO Auto-generated method stub

	}

	public synchronized Object connect(ID targetID, Object data, int timeout)
			throws ECFException {
		if (targetID == null) throw new ECFException("targetID cannot be null");
		if (!(targetID instanceof PahoID)) throw new ECFException("targetID must be in PahoID namespace");
		PahoID pahoTargetID = (PahoID) targetID;
		
		MqttConnectOptions connectOpts = createConnectionOptions(targetID, data, timeout);
		String serverURI = null;
		String clientId = null;
		String topic = null;
		this.mqttTopic = null;
		// connect to broker
		try {
			this.client = new MqttClient(serverURI, clientId);
			this.client.setCallback(callback);
			this.client.connect(connectOpts);
			this.client.subscribe(topic);
			byte[] connectPayload = null;
			this.client.publish(mqttTopic, new MqttMessage(connectPayload));
		} catch (MqttSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO Auto-generated method stub
		return null;
	}

	private MqttConnectOptions createConnectionOptions(ID targetID,
			Object data, int timeout) {
		MqttConnectOptions options = new MqttConnectOptions();
		options.setCleanSession(true);
		options.setConnectionTimeout(timeout);
		options.setKeepAliveInterval(timeout);
		return options;
	}

	public void disconnect() {
		// TODO Auto-generated method stub

	}

	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	public ID getLocalID() {
		return clientID;
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
