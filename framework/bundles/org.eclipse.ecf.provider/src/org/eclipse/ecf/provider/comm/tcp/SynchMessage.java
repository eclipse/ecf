package org.eclipse.ecf.provider.comm.tcp;

import java.io.Serializable;

public class SynchMessage extends AsynchMessage {

    protected SynchMessage(Serializable data) {
        super(data);
    }
    protected SynchMessage() {
        super();
    }
    public String toString() {
    	StringBuffer buf = new StringBuffer("SynchMessage[");
    	buf.append(data).append("]");
    	return buf.toString();
    }
}