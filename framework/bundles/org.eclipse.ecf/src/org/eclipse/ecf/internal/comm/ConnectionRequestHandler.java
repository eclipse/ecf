package org.eclipse.ecf.internal.comm;

import java.io.Serializable;
import java.net.Socket;

public interface ConnectionRequestHandler {

    public Serializable checkConnect(Socket aSocket, String target, Serializable data, ISynchAsynchConnection conn);
    
}
