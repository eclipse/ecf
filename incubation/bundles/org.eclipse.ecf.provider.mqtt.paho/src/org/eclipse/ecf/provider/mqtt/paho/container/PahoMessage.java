package org.eclipse.ecf.provider.mqtt.paho.container;

import java.io.IOException;
import java.io.Serializable;

import org.eclipse.ecf.provider.generic.SOContainer;
import org.eclipse.ecf.provider.mqtt.paho.identity.PahoID;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class PahoMessage implements Serializable {

	private static final long serialVersionUID = -8768793858838034483L;

	private PahoID targetID;
	private byte[] bytes;
	
	public PahoMessage(PahoID targetID, byte[] bytes) {
		this.targetID = targetID;
		this.bytes = bytes;
	}
	
	public PahoID getTargetID() {
		return this.targetID;
	}
	
	public byte[] getBytes() {
		return this.bytes;
	}
	
	public MqttMessage createMessage() throws IOException {
		byte[] objectBytes = SOContainer.serialize(this);
		return new MqttMessage(objectBytes);
	}
}
