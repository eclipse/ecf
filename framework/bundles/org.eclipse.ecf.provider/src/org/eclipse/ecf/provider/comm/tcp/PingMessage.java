package org.eclipse.ecf.provider.comm.tcp;

import java.io.Serializable;

public class PingMessage implements Serializable {
    protected PingMessage() {
    }
    public String toString() {
    	StringBuffer buf = new StringBuffer("PingMessage");
    	return buf.toString();
    }
}