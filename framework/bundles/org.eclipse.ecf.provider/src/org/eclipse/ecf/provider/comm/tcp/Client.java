/****************************************************************************
* Copyright (c) 2004 Composent, Inc. and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Composent, Inc. - initial API and implementation
*****************************************************************************/

package org.eclipse.ecf.provider.comm.tcp;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import org.eclipse.ecf.core.comm.AsynchConnectionEvent;
import org.eclipse.ecf.core.comm.ConnectionDescription;
import org.eclipse.ecf.core.comm.ConnectionEvent;
import org.eclipse.ecf.core.comm.ConnectionInstantiationException;
import org.eclipse.ecf.core.comm.DisconnectConnectionEvent;
import org.eclipse.ecf.core.comm.IConnectionEventHandler;
import org.eclipse.ecf.core.comm.ISynchAsynchConnection;
import org.eclipse.ecf.core.comm.ISynchAsynchConnectionEventHandler;
import org.eclipse.ecf.core.comm.SynchConnectionEvent;
import org.eclipse.ecf.core.comm.provider.ISynchAsynchConnectionInstantiator;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.util.SimpleQueueImpl;
import org.eclipse.ecf.provider.Trace;

public final class Client implements ISynchAsynchConnection {
    public static class Creator implements ISynchAsynchConnectionInstantiator {
        public ISynchAsynchConnection makeInstance(ConnectionDescription description,
                ISynchAsynchConnectionEventHandler handler, Class[] clazzes,
                Object[] args) throws ConnectionInstantiationException {
            try {
                String [] argVals = description.getArgDefaults();
                Integer ka = null;
                if (argVals != null && argVals.length > 0) {
                    String val = argVals[0];
                    if (val != null) {
                        ka = new Integer(val);
                    }
                }
                if (args != null && args.length > 0) {
                    if (args[0] instanceof Integer) {
                        ka = (Integer) args[0];
                    } else if (args[0] instanceof String) {
                        ka = new Integer((String) args[0]);
                    }
                }
                return new Client(handler, ka);
            } catch (Exception e) {
                throw new ConnectionInstantiationException(
                        "Exception in creating connection "
                                + Client.class.getName(), e);
            }
        }
    }

    public static final String PROTOCOL = "ecftcp";
    public static final Trace debug = Trace.create("connection");
    public static final int SNDR_PRIORITY = Thread.NORM_PRIORITY;
    public static final int RCVR_PRIORITY = Thread.NORM_PRIORITY;
    // Default close timeout is 1.5 seconds
    public static final long CLOSE_TIMEOUT = 1500;
    public static final int DEF_MAX_MSG = 50;
    protected String address;
    protected int port;
    protected Socket socket;
    protected ObjectOutputStream outputStream;
    protected ObjectInputStream inputStream;
    protected ISynchAsynchConnectionEventHandler handler;
    protected SimpleQueueImpl queue = new SimpleQueueImpl();
    protected int keepAlive = 0;
    protected Thread sendThread;
    protected Thread rcvThread;
    protected Thread keepAliveThread;
    protected boolean isClosing = false;
    protected boolean waitForPing = false;
    protected PingMessage ping = new PingMessage();
    protected PingResponseMessage pingResp = new PingResponseMessage();
    protected long nextPingTime;
    protected int maxMsg = DEF_MAX_MSG;
    protected long closeTimeout = CLOSE_TIMEOUT;
    protected Vector eventNotify = null;
    protected Map properties;

    public Map getProperties() {
        return properties;
    }
    public Object getAdapter(Class clazz) {
        return null;
    }
    protected void debug(String msg) {
        if (Trace.ON && debug != null) {
            debug.msg(msg);
        }
    }

    protected void dumpStack(String msg, Throwable e) {
        if (Trace.ON && debug != null) {
            debug.dumpStack(e, msg);
        }
    }

    public void setProperties(Map props) {
        this.properties = props;
    }

    public Client(Socket aSocket, ObjectInputStream iStream,
            ObjectOutputStream oStream,
            ISynchAsynchConnectionEventHandler handler, int keepAlive)
            throws IOException {
        this(aSocket, iStream, oStream, handler, keepAlive, DEF_MAX_MSG);
    }

    public Client(Socket aSocket, ObjectInputStream iStream,
            ObjectOutputStream oStream,
            ISynchAsynchConnectionEventHandler handler, int keepAlive,
            int maxmsgs) throws IOException {
        socket = aSocket;
        // Set TCP no delay
        socket.setTcpNoDelay(true);
        address = socket.getInetAddress().getHostName();
        port = socket.getPort();
        inputStream = iStream;
        outputStream = oStream;
        this.handler = handler;
        this.keepAlive = keepAlive;
        maxMsg = maxmsgs;
        properties = new Properties();
        setupThreads();
    }

    public Client(ISynchAsynchConnectionEventHandler handler, Integer keepAlive) {
        this(handler, keepAlive.intValue());
    }

    public Client(ISynchAsynchConnectionEventHandler handler, int keepAlive) {
        this(handler, keepAlive, DEF_MAX_MSG);
    }

    public Client(ISynchAsynchConnectionEventHandler handler, int keepAlive,
            int maxmsgs) {
        this.handler = handler;
        this.keepAlive = keepAlive;
        maxMsg = maxmsgs;
        this.properties = new Properties();
    }

    public synchronized ID getLocalID() {
        if (socket == null)
            return null;
        ID retID = null;
        try {
            retID = IDFactory.makeStringID(PROTOCOL + "://"
                    + socket.getLocalAddress().getHostName() + ":" + port);
        } catch (Exception e) {
            return null;
        }
        return retID;
    }

    public synchronized void removeCommEventListener(IConnectionEventHandler l) {
        eventNotify.remove(l);
    }

    public synchronized void addCommEventListener(IConnectionEventHandler l) {
        if (eventNotify == null) {
            eventNotify = new Vector();
        }
        eventNotify.add(l);
    }

    public synchronized boolean isConnected() {
        if (socket != null) {
            return socket.isConnected();
        }
        return false;
    }

    public synchronized boolean isStarted() {
        if (sendThread != null) {
            return sendThread.isAlive();
        }
        return false;
    }

    protected void fireSuspect(Exception e) {
        Vector v = null;
        synchronized (this) {
            if (eventNotify == null)
                return;
            v = eventNotify;
        }
        for (Enumeration en = v.elements(); en.hasMoreElements();) {
            IConnectionEventHandler h = (IConnectionEventHandler) en
                    .nextElement();
            h.handleSuspectEvent(new ConnectionEvent(this, e));
        }
    }

    public synchronized Object connect(ID remote, Object data, int timeout)
            throws IOException {
        debug("connect(" + remote + "," + data + "," + timeout + ")");
        if (socket != null)
            throw new ConnectException("ClientApplication already connected");
        URI anURI = null;
        try {
            anURI = remote.toURI();
        } catch (URISyntaxException e) {
            throw new IOException("Can't connect to address "
                    + remote.getName() + ". Invalid URL");
        }
        address = anURI.getHost();
        port = anURI.getPort();
        SocketFactory fact = SocketFactory.getSocketFactory();
        if (fact == null) {
            fact = SocketFactory.getDefaultSocketFactory();
        }
        debug("socket connecting to " + address + ":" + port);
        // Actually connect to remote using socket from socket factory.
        socket = fact.createSocket(address, port, timeout);
        // Set TCP no delay
        socket.setTcpNoDelay(true);
        boolean compatibility = false;
        //boolean compatibility = TCPCompatibility.useCompatibility;
        outputStream = new ExObjectOutputStream(new BufferedOutputStream(socket
                .getOutputStream()), compatibility);
        outputStream.flush();
        inputStream = new ExObjectInputStream(socket.getInputStream(),
                compatibility);
        // send connect data
        debug("send:" + address + ":" + port + ":" + data);
        sendIt(new ConnectRequestMessage(anURI, (Serializable) data));
        ConnectResultMessage res = null;
        res = (ConnectResultMessage) readObject();
        debug("recv:" + address + ":" + port + ":" + res);
        // Setup threads
        setupThreads();
        // Return results.
        return res.getData();
    }

    private void setupThreads() {
        // Setup threads
        sendThread = (Thread) AccessController
                .doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        return getSendThread();
                    }
                });
        rcvThread = (Thread) AccessController
                .doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        return getRcvThread();
                    }
                });
    }

    private Thread getSendThread() {
        Thread aThread = new Thread(new Runnable() {
            public void run() {
                int msgCount = 0;
                Thread me = Thread.currentThread();
                // Loop until done sending messages
                for (;;) {
                    if (me.isInterrupted())
                        break;
                    Serializable aMsg = (Serializable) queue.peekQueue();
                    if (me.isInterrupted() || aMsg == null)
                        break;
                    try {
                        // Actually send message
                        debug("send:" + address + ":" + port + ":" + aMsg);
                        sendIt(aMsg);
                        // Successful...remove message from queue
                        queue.removeHead();
                        if (msgCount > maxMsg) {
                            synchronized (outputStream) {
                                outputStream.reset();
                            }
                            msgCount = 0;
                        } else
                            msgCount++;
                    } catch (IOException e) {
                        if (isClosing) {
                            isClosing = false;
                            synchronized (Client.this) {
                                Client.this.notifyAll();
                            }
                        } else {
                            if (!handler.handleSuspectEvent(new ConnectionEvent(
                                    Client.this, e))) {
                                handler
                                        .handleDisconnectEvent(new DisconnectConnectionEvent(
                                                Client.this, e, queue));
                            }
                        }
                        break;
                    }
                }
                debug("sender:" + address + ":" + port + " terminating");
            }
        }, "sndr:" + address + ":" + port);
        // Set priority for new thread
        aThread.setPriority(SNDR_PRIORITY);
        return aThread;
    }

    private void closeSocket() {
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
        }
    }

    private void sendIt(Serializable snd) throws IOException {
        // Write object to output stream
        synchronized (outputStream) {
            outputStream.writeObject(snd);
            outputStream.flush();
            nextPingTime = System.currentTimeMillis() + keepAlive;
        }
    }

    private void receiveResp() {
        synchronized (outputStream) {
            waitForPing = false;
            nextPingTime = System.currentTimeMillis() + keepAlive;
            outputStream.notifyAll();
        }
    }

    public void setCloseTimeout(long t) {
        closeTimeout = t;
    }

    private void sendClose(Serializable snd) throws IOException {
        isClosing = true;
        debug("send:" + address + ":" + port + ":" + snd);
        sendIt(snd);
        if (isClosing) {
            try {
                wait(closeTimeout);
            } catch (Exception e) {
            }
        }
        // Before returning, actually remove remote objects
        //handler.handleDisconnectEvent(new DisconnectConnectionEvent(
        //ClientApplication.this, null, queue));
    }

    private Thread getRcvThread() {
        Thread aThread = new Thread(new Runnable() {
            public void run() {
                Thread me = Thread.currentThread();
                // Loop forever and handle objects received.
                for (;;) {
                    if (me.isInterrupted())
                        break;
                    try {
                        handleRcv(readObject());
                    } catch (IOException e) {
                        if (isClosing) {
                            isClosing = false;
                            synchronized (Client.this) {
                                Client.this.notifyAll();
                            }
                        } else {
                            if (!handler.handleSuspectEvent(new ConnectionEvent(
                                    Client.this, e))) {
                                handler
                                        .handleDisconnectEvent(new DisconnectConnectionEvent(
                                                Client.this, e, queue));
                            }
                        }
                        break;
                    }
                }
                debug("read:" + address + ":" + port + " terminating");
            }
        }, "rcvr:" + address + ":" + port);
        // Set priority and return
        aThread.setPriority(RCVR_PRIORITY);
        return aThread;
    }

    private void handleRcv(Serializable rcv) throws IOException {
        try {
            // We've received some data, so the connection is alive
            receiveResp();
            // Handle all messages
            if (rcv instanceof SynchMessage) {
                // Handle synch message. The only valid synch message is
                // 'close'.
                debug("recv:" + address + ":" + port + ":" + rcv);
                handler.handleSynchEvent(new SynchConnectionEvent(this,
                        ((SynchMessage) rcv).getData()));
            } else if (rcv instanceof AsynchMessage) {
                debug("recv:" + address + ":" + port + ":" + rcv);
                Serializable d = ((AsynchMessage) rcv).getData();
                // Handle asynch messages.
                handler.handleAsynchEvent(new AsynchConnectionEvent(this, d));
            } else if (rcv instanceof PingMessage) {
                // Handle ping by sending response back immediately
                sendIt(pingResp);
            } else if (rcv instanceof PingResponseMessage) {
                // Do nothing with ping response
            } else
                throw new IOException("Invalid message received.");
        } catch (IOException e) {
            disconnect();
            throw e;
        }
    }

    public synchronized void start() {
        debug("start(" + address + ":" + port + ")");
        if (sendThread != null)
            sendThread.start();
        if (rcvThread != null)
            rcvThread.start();
        // Setup and start keep alive thread
        if (keepAlive != 0)
            keepAliveThread = setupPing();
        if (keepAliveThread != null)
            keepAliveThread.start();
    }

    public void stop() {
    }

    private Thread setupPing() {
        return new Thread(new Runnable() {
            public void run() {
                Thread me = Thread.currentThread();
                while (!queue.isStopped()) {
                    try {
                        if (me.isInterrupted())
                            break;
                        // Sleep for timeout interval divided by two
                        Thread.sleep(keepAlive / 2);
                        if (me.isInterrupted())
                            break;
                        // Check to see how long it has been since our last
                        // send.
                        synchronized (outputStream) {
                            if (System.currentTimeMillis() >= nextPingTime) {
                                // If it's been longer than our timeout
                                // interval, then ping
                                waitForPing = true;
                                // Actually send ping instance
                                sendIt(ping);
                                if (waitForPing) {
                                    try {
                                        // Wait for keepAliveInterval for
                                        // pingresp
                                        outputStream.wait(keepAlive / 2);
                                    } catch (InterruptedException e) {
                                    }
                                }
                                if (waitForPing) {
                                    throw new IOException(address + ":" + port
                                            + " not reachable.");
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (isClosing) {
                            isClosing = false;
                            synchronized (Client.this) {
                                Client.this.notifyAll();
                            }
                        } else {
                            if (!handler.handleSuspectEvent(new ConnectionEvent(
                                    Client.this, e))) {
                                handler
                                        .handleDisconnectEvent(new DisconnectConnectionEvent(
                                                Client.this, e, queue));
                            }
                        }
                        break;
                    }
                }
            }
        }, "ping:" + address + ":" + port);
    }

    public synchronized void disconnect() throws IOException {
        debug("disconnect(" + address + ":" + port + ")");
        // Close send queue and socket
        queue.close();
        closeSocket();
        // Notify sender in case it's waiting for a response
        // Zap keep alive thread
        if (keepAliveThread != null) {
            keepAliveThread.interrupt();
            keepAliveThread = null;
        }
        if (sendThread != null) {
            sendThread.interrupt();
            sendThread = null;
        }
        if (rcvThread != null) {
            rcvThread.interrupt();
            rcvThread = null;
        }
        // Notify any threads waiting to get hold of our lock
        notifyAll();
    }

    public void sendAsynch(ID recipient, byte[] obj) throws IOException {
        queueObject(recipient, obj);
    }

    public void sendAsynch(ID recipient, Object obj) throws IOException {
        queueObject(recipient, (Serializable) obj);
    }

    public synchronized void queueObject(ID recipient, Serializable obj)
            throws IOException {
        if (queue.isStopped() || isClosing)
            throw new ConnectException("Not connected");
        queue.enqueue(new AsynchMessage(obj));
    }

    public synchronized Serializable sendObject(ID recipient, Serializable obj)
            throws IOException {
        if (queue.isStopped() || isClosing)
            throw new ConnectException("Not connected");
        sendClose(new SynchMessage(obj));
        return null;
    }

    public Object sendSynch(ID rec, Object obj) throws IOException {
        return sendObject(rec, (Serializable) obj);
    }

    public Object sendSynch(ID rec, byte[] obj) throws IOException {
        return sendObject(rec, obj);
    }

    private Serializable readObject() throws IOException {
        Serializable ret = null;
        try {
            ret = (Serializable) inputStream.readObject();
        } catch (ClassNotFoundException e) {
            dumpStack("ClassNotFoundException", e);
            throw new IOException(
                    "Protocol violation due to class load failure.  "
                            + e.getMessage());
        }
        return ret;
    }
}