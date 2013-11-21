package org.eclipse.ecf.provider.mqtt.paho.container;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.eclipse.ecf.provider.mqtt.paho.identity.PahoID;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class PahoMessage implements Serializable {

	private static final long serialVersionUID = -8768793858838034483L;

	private PahoID fromID;
	private PahoID targetID;
	private Serializable data;

	public static byte[] serialize(Object object) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(object);
		return bos.toByteArray();
	}

	public static Object deserialize(byte[] bytes) throws IOException,
			ClassNotFoundException {
		ObjectInputStream oos = new ObjectInputStream(new ByteArrayInputStream(
				bytes));
		return oos.readObject();
	}

	public PahoMessage(PahoID fromID, PahoID targetID, Serializable data) {
		if (fromID == null)
			throw new IllegalArgumentException("fromID cannot be null");
		this.fromID = fromID;
		this.targetID = targetID;
		this.data = data;
	}

	public PahoID getFromID() {
		return this.fromID;
	}

	public PahoID getTargetID() {
		return this.targetID;
	}

	public Serializable getData() {
		return this.data;
	}

	public byte[] serialize() throws IOException {
		return serialize(this);
	}

	public MqttMessage toMqttMessage() throws IOException {
		return new MqttMessage(serialize());
	}
}
