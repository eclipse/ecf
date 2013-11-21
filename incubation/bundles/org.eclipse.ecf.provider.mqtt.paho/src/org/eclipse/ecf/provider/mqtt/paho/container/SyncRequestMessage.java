package org.eclipse.ecf.provider.mqtt.paho.container;

import java.io.Serializable;

import org.eclipse.ecf.provider.mqtt.paho.identity.PahoID;

public class SyncRequestMessage extends PahoMessage {

	private static final long serialVersionUID = 9073885283936597534L;

	public SyncRequestMessage(PahoID fromID, PahoID targetID, Serializable data) {
		super(fromID, targetID, data);
	}

}
