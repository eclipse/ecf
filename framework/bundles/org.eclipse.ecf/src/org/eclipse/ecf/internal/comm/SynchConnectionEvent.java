package org.eclipse.ecf.internal.comm;


public class SynchConnectionEvent extends ConnectionEvent {
	public SynchConnectionEvent(ISynchConnection conn, Object data) {
		super(conn, data);
	}
}
