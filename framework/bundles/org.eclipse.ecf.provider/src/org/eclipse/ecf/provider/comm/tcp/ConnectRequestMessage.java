package org.eclipse.ecf.provider.comm.tcp;

import java.io.Serializable;
import java.net.URI;

public class ConnectRequestMessage implements Serializable {

    URI target;
    Serializable data;

    public ConnectRequestMessage(URI target, Serializable data) {
        this.target = target;
        this.data = data;
    }

    public URI getTarget() {
        return target;
    }

    public Serializable getData() {
        return data;
    }

    public String toString() {
        return "ConnectRequestMessage[target:" + target + ";data:" + data + "]";
    }
}