package org.eclipse.ecf.provider.comm.tcp;

import java.io.Serializable;

public class ConnectResultMessage implements Serializable {

    Serializable data;

    public ConnectResultMessage(Serializable data) {
        this.data = data;
    }

    public Serializable getData() {
        return data;
    }
    
    public String toString() {
    	StringBuffer buf = new StringBuffer("ConnectResultMessage[");
    	buf.append(data).append("]");
    	return buf.toString();
    }
}