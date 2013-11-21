package org.eclipse.ecf.provider.mqtt.paho.container;

import java.io.IOException;

import org.eclipse.ecf.provider.mqtt.paho.identity.PahoID;

public class ConnectRequestMessage extends PahoMessage {

	private static final long serialVersionUID = 3750692684824242655L;

	public ConnectRequestMessage(PahoID fromID, PahoID targetID, Object data)
			throws IOException {
		super(fromID, targetID, serialize(data));
	}

}
