package org.eclipse.ecf.provider.comm.tcp;

import java.io.Serializable;

public class AsynchMessage implements Serializable {

    Serializable data;

    protected AsynchMessage() {
    }

    protected AsynchMessage(Serializable data) {
        this.data = data;
    }
    Serializable getData() {
        return data;
    }
    public String toString() {
    	StringBuffer buf = new StringBuffer("AsynchMessage[");
    	buf.append(data).append("]");
    	return buf.toString();
    }
}