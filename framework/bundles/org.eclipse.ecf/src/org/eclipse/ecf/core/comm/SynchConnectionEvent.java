package org.eclipse.ecf.core.comm;

public class SynchConnectionEvent extends ConnectionEvent {
    public SynchConnectionEvent(ISynchConnection conn, Object data) {
        super(conn, data);
    }
}