package org.eclipse.ecf.internal.comm;

import org.eclipse.ecf.core.util.Event;

public class ConnectionEvent implements Event {

	Object odata = null;
	byte [] bdata = null;
	IConnection connection = null;
	
	public ConnectionEvent(IConnection source, Object odata) {
	    this.connection = source;
		this.odata = odata;
	}
	public ConnectionEvent(IConnection source, byte [] bdata) {
	    this.connection = source;
	    this.bdata = bdata;
	}
	public IConnection getConnection() {
		return connection;
	}
	public Object getOData() {
		return odata;
	}
	public byte [] getBData() {
	    return bdata;
	}

}
