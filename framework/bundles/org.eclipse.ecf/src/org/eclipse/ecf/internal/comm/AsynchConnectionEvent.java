package org.eclipse.ecf.internal.comm;

public class AsynchConnectionEvent extends ConnectionEvent {
	public AsynchConnectionEvent(IAsynchConnection conn, Object data) {
		super(conn, data);
	}
}