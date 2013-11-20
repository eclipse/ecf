package org.eclipse.ecf.provider.mqtt.paho.container;

import org.eclipse.ecf.provider.mqtt.paho.identity.PahoID;

public class SyncPahoRequest extends PahoMessage {

	private static final long serialVersionUID = 9073885283936597534L;

	public SyncPahoRequest(PahoID targetID, byte[] bytes) {
		super(targetID, bytes);
	}

}
