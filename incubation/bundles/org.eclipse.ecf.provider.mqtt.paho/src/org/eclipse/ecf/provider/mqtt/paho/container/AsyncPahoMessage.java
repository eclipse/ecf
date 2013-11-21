package org.eclipse.ecf.provider.mqtt.paho.container;

import java.io.Serializable;

import org.eclipse.ecf.provider.mqtt.paho.identity.PahoID;

public class AsyncPahoMessage extends PahoMessage {

	private static final long serialVersionUID = -2538908397359614161L;

	public AsyncPahoMessage(PahoID fromID, PahoID targetID, Serializable data) {
		super(fromID, targetID, data);
	}

}
