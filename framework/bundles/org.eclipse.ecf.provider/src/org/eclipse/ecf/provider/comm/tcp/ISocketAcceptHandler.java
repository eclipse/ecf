package org.eclipse.ecf.provider.comm.tcp;

import java.net.Socket;

public interface ISocketAcceptHandler {
    public void handleAccept(Socket aSocket) throws Exception;
}