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
}