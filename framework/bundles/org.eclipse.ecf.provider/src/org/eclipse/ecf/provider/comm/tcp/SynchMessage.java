package org.eclipse.ecf.provider.comm.tcp;

import java.io.Serializable;

public class SynchMessage extends AsynchMessage {

    protected SynchMessage(Serializable data) {
        super(data);
    }
    protected SynchMessage() {
        super();
    }
}