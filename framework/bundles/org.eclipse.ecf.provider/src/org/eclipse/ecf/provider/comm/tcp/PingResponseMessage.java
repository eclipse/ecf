package org.eclipse.ecf.provider.comm.tcp;

import java.io.Serializable;

public class PingResponseMessage implements Serializable {
    protected PingResponseMessage() {
    }
    public String toString() {
    	StringBuffer buf = new StringBuffer("PingResponseMessage");
    	return buf.toString();
    }
}