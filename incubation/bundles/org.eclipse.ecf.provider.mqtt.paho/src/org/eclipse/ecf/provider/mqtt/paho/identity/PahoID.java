package org.eclipse.ecf.provider.mqtt.paho.identity;

import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.identity.StringID;

public class PahoID extends StringID {

	private static final long serialVersionUID = 9124480553723803576L;

	public PahoID(Namespace n, String s) {
		super(n, s);
	}

	public String getTargetID() {
		// XXX TODO
		return null;
	}
	
	public String getClientID() {
		// XXX TODO
		return null;
	}
}
