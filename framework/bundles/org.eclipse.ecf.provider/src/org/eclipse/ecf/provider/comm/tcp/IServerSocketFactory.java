package org.eclipse.ecf.provider.comm.tcp;

import java.io.IOException;
import java.net.ServerSocket;

public interface IServerSocketFactory {
    ServerSocket createServerSocket(int port, int backlog) throws IOException;
}

