package org.eclipse.ecf.provider.mqtt.paho.container;

import java.io.IOException;
import java.util.Map;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.provider.comm.IConnectionListener;
import org.eclipse.ecf.provider.mqtt.paho.identity.PahoID;
import org.eclipse.ecf.provider.mqtt.paho.identity.PahoNamespace;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public abstract class AbstractPahoConnection {

	protected PahoID clientID;
	protected MqttClient client;
	protected String mqttTopic;
	protected boolean connected;
	
	protected String getMqttTopic() {
		return this.mqttTopic;
	}
	
	public AbstractPahoConnection(PahoID clientID) {
		this.clientID = clientID;
	}
	
	protected MqttCallback callback = new MqttCallback() {
		public void connectionLost(Throwable cause) {
			handleConnectionLost(cause);
		}

		public void messageArrived(String topic, MqttMessage message)
				throws Exception {
			handleMessageArrived(topic, message);
		}

		public void deliveryComplete(IMqttDeliveryToken token) {
			handleDeliveryComplete(token);
		}
	};

	protected void handleConnectionLost(Throwable cause) {
		// xxx todo
	}

	protected void handleDeliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
	}

	protected void handleMessageArrived(String topic2, MqttMessage message) {
		// TODO Auto-generated method stub
	}

	protected synchronized void connectAndSubscribe(PahoID targetID, MqttConnectOptions opts) throws ECFException {
		// Create client
		try {
			this.client = new MqttClient(targetID.getServerURI(), targetID.getClientId());
			// Set callback
			this.client.setCallback(callback);
			// Connect to broker with connectOpts
			if (opts == null)
				this.client.connect();
			else
				this.client.connect(opts);
			// Subscribe to topic
			this.client.subscribe(targetID.getClientId());
			this.connected = true;
		} catch (MqttException e) {
			throw new ECFException("Could not connect to targetID" + targetID.getName());
		}
	}
	
	@SuppressWarnings("rawtypes")
	public Map getProperties() {
		return null;
	}
	public void addListener(IConnectionListener listener) {
	}
	public void removeListener(IConnectionListener listener) {
	}

	public ID getLocalID() {
		return clientID;
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}

	protected MqttConnectOptions createConnectionOptions(ID targetID,
			Object data, int timeout) {
		MqttConnectOptions options = new MqttConnectOptions();
		options.setCleanSession(true);
		options.setConnectionTimeout(timeout);
		options.setKeepAliveInterval(timeout);
		return options;
	}

	public boolean isConnected() {
		return connected;
	}

	protected void publishMessage(PahoMessage message) throws IOException {
		try {
			this.client.publish(getMqttTopic(), message.createMessage());
		} catch (MqttException e) {
			throw new IOException(e.getMessage(),e);
		}
	}
	
	public synchronized void disconnect() {
		if (isConnected()) {
			try {
				this.client.disconnect();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.client = null;
			this.connected = false;
		}
	}

	public synchronized void sendAsynch(ID receiver, byte[] data)
			throws IOException {
		if (!isConnected()) throw new IOException("PahoConnection not connected");
		if (receiver != null && !PahoNamespace.NAME.equals(receiver.getNamespace().getName())) throw new IOException("receiver not in PahoID namespace");
		publishMessage(new PahoMessage((PahoID) receiver, data));
	}

}
