package org.eclipse.ecf.internal.comm;


public class DisconnectConnectionEvent extends ConnectionEvent {

    Throwable exception = null;

	public DisconnectConnectionEvent(
		IAsynchConnection conn,
		Throwable e,
		Object data) {
		super(conn, data);
		exception = e;
	}

	public Throwable getException() {
		return exception;
	}
}
