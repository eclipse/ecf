package org.eclipse.ecf.internal.comm;

import org.eclipse.ecf.core.util.Event;

public class ConnectionEvent implements Event {

	Object data = null;
	IConnection connection = null;
	
	public ConnectionEvent(IConnection source, Object data) {
	    this.connection = source;
	    this.data = data;
	}
	public IConnection getConnection() {
		return connection;
	}
	public Object getData() {
	    return data;
	}

}
