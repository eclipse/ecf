package org.eclipse.ecf.internal.comm;

import java.io.Serializable;

public class SynchConnectionEvent extends ConnectionEvent {
	public SynchConnectionEvent(ISynchConnection conn, Serializable data) {
		super(conn, data);
	}
}
