package org.eclipse.ecf.provider.comm.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.eclipse.ecf.provider.Debug;

public class Server extends ServerSocket {
    public static Debug debug = Debug.create(Server.class.getName());

    ISocketAcceptHandler acceptHandler;

    Thread listenerThread;
    ThreadGroup threadGroup;

    public Server(ThreadGroup group, int port, ISocketAcceptHandler handler)
            throws IOException {
        super(port);
        if (handler == null)
            throw new InstantiationError("Listener cannot be null");
        acceptHandler = handler;
        threadGroup = group;
        listenerThread = setupListener();
        listenerThread.start();
    }

    public Server(int port, ISocketAcceptHandler handler) throws IOException {
        this(null, port, handler);
    }

    protected Thread setupListener() {
        return new Thread(threadGroup, new Runnable() {
            public void run() {
                while (true) {
                    try {
                        handleAccept(accept());
                    } catch (Exception e) {
                        if (Debug.ON && debug != null) {
                            debug.dumpStack(e, "Exception in accept");
                        }
                        // If we get an exception on accept(), we should just
                        // exit
                        break;
                    }
                }
                if (Debug.ON && debug != null) {
                    debug.msg("Closing listener normally.");
                }
            }
        }, "Server(" + getLocalPort() + ")");
    }

    protected void handleAccept(final Socket aSocket) {
        new Thread(threadGroup, new Runnable() {
            public void run() {
                try {
                    acceptHandler.handleAccept(aSocket);
                } catch (Exception e) {
                    if (Debug.ON && debug != null) {
                        debug.dumpStack(e,
                                "Unexplained exception in connect.  Closing.");
                    }
                    try {
                        aSocket.close();
                    } catch (IOException e1) {
                    }
                    ;
                } finally {
                    if (Debug.ON && debug != null) {
                        debug.msg("handleAcceptAsych terminating.");
                    }
                }
            }
        }).start();
    }

    public synchronized void close() throws IOException {
        if (Debug.ON && debug != null) {
            debug.msg("close()");
        }
        super.close();
        if (listenerThread != null) {
            listenerThread.interrupt();
            listenerThread = null;
        }
        if (threadGroup != null) {
            threadGroup.interrupt();
            threadGroup = null;
        }
        acceptHandler = null;
    }
}