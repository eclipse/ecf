package org.eclipse.ecf.provider.comm.tcp;

import java.io.IOException;
import java.net.Socket;

public interface IClientSocketFactory {
    Socket createSocket(String name, int port, int timeout) throws IOException;
}