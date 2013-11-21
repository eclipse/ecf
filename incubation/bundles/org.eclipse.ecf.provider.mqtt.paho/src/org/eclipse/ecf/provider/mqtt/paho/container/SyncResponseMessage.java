package org.eclipse.ecf.provider.mqtt.paho.container;

import java.io.Serializable;

import org.eclipse.ecf.provider.mqtt.paho.identity.PahoID;

public class SyncResponseMessage extends PahoMessage {

	private static final long serialVersionUID = 4085439339371979310L;

	public SyncResponseMessage(PahoID fromID, PahoID targetID, Serializable data) {
		super(fromID, targetID, data);
	}

}
