package org.eclipse.ecf.provider.generic;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.URI;

import org.eclipse.ecf.provider.Debug;
import org.eclipse.ecf.core.comm.ConnectionRequestHandler;
import org.eclipse.ecf.provider.comm.tcp.Client;
import org.eclipse.ecf.provider.comm.tcp.ConnectRequestMessage;
import org.eclipse.ecf.provider.comm.tcp.ConnectResultMessage;
import org.eclipse.ecf.provider.comm.tcp.ExObjectInputStream;
import org.eclipse.ecf.provider.comm.tcp.ExObjectOutputStream;
import org.eclipse.ecf.provider.comm.tcp.ISocketAcceptHandler;
import org.eclipse.ecf.provider.comm.tcp.Server;

public class TCPServerSOContainerGroup extends SOContainerGroup implements
        ISocketAcceptHandler {

    public static final String INVALID_CONNECT = "Invalid connect request.  ";

    public static final Debug debug = Debug
            .create(TCPServerSOContainerGroup.class.getName());
    public static final String DEFAULT_GROUP_NAME = TCPServerSOContainerGroup.class
            .getName();

    protected int port;
    Server listener;
    boolean isOnTheAir = false;
    ThreadGroup threadGroup;

    public TCPServerSOContainerGroup(String name, ThreadGroup group, int port) {
        super(name);
        threadGroup = group;
        this.port = port;
    }

    public TCPServerSOContainerGroup(String name, int port) {
        this(name, null, port);
    }

    public TCPServerSOContainerGroup(int port) {
        this(DEFAULT_GROUP_NAME, null, port);
    }

    public synchronized void putOnTheAir() throws IOException {
        if (Debug.ON && debug != null) {
            debug.msg("Putting group " + this + " on the air.");
        }
        listener = new Server(threadGroup, port, this);
        port = listener.getLocalPort();
        isOnTheAir = true;
    }

    public synchronized boolean isOnTheAir() {
        return isOnTheAir;
    }

    public void handleAccept(Socket aSocket) throws Exception {
        ObjectOutputStream oStream = new ExObjectOutputStream(
                new BufferedOutputStream(aSocket.getOutputStream()));
        oStream.flush();

        ObjectInputStream iStream = new ExObjectInputStream(aSocket
                .getInputStream());

        ConnectRequestMessage req = (ConnectRequestMessage) iStream
                .readObject();
        if (Debug.ON && debug != null) {
            debug.msg("Got connect request " + req);
        }
        if (req == null)
            throw new InvalidObjectException(INVALID_CONNECT
                    + "ConnectRequestMessage is null");

        URI uri = req.getTarget();
        if (uri == null)
            throw new InvalidObjectException(INVALID_CONNECT
                    + "Target URI is null");
        String path = uri.getPath();
        if (path == null)
            throw new InvalidObjectException(INVALID_CONNECT
                    + "Target path is null");

        TCPServerSOContainer srs = (TCPServerSOContainer) get(path);
        if (Debug.ON && debug != null) {
            debug.msg("Found container with " + srs.getID().getName()
                    + " for target " + uri);
        }
        if (srs == null)
            throw new InvalidObjectException("Container for target " + path
                    + " not found!");

        // Create our local messaging interface
        Client newClient = new Client(aSocket, iStream, oStream, srs
                .getReceiver(), srs.keepAlive);

        // No other threads can access messaging interface until space has
        // accepted/rejected
        // connect request
        synchronized (newClient) {
            // Call checkConnect
            Serializable resp = (Serializable) ((ConnectionRequestHandler) srs)
                    .checkConnect(aSocket, path, req.getData(), newClient);
            // Create connect response wrapper and send it back
            oStream.writeObject(new ConnectResultMessage(resp));
            oStream.flush();
        }
    }

    public synchronized void takeOffTheAir() {
        if (listener != null) {
            if (Debug.ON && debug != null) {
                debug.msg("Taking " + getName() + " on the air.");
            }
            try {
                listener.close();
            } catch (IOException e) {
                if (Debug.ON && debug != null) {
                    debug.dumpStack(e, "Exception in closeListener");
                }
            }
            listener = null;
        }
        isOnTheAir = false;
    }

    public int getPort() {
        return port;
    }

    public String toString() {
        return super.toString() + ";port:" + port;
    }
}