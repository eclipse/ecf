package org.eclipse.ecf.provider.mqtt.paho.container;

import org.eclipse.ecf.provider.mqtt.paho.identity.PahoID;

public class SyncPahoResponse extends PahoMessage {

	private static final long serialVersionUID = 4085439339371979310L;

	public SyncPahoResponse(PahoID targetID, byte[] bytes) {
		super(targetID, bytes);
	}

}
