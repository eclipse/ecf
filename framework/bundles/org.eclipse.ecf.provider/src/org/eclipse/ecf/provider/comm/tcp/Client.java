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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.util.SimpleQueueImpl;
import org.eclipse.ecf.internal.provider.Trace;
import org.eclipse.ecf.provider.comm.AsynchEvent;
import org.eclipse.ecf.provider.comm.DisconnectEvent;
import org.eclipse.ecf.provider.comm.IConnectionListener;
import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
import org.eclipse.ecf.provider.comm.ISynchAsynchEventHandler;
import org.eclipse.ecf.provider.comm.SynchEvent;

public final class Client implements ISynchAsynchConnection {
	public static final String PROTOCOL = "ecftcp";
	protected static final Trace trace = Trace.create("connection");
	public static final int DEFAULT_SNDR_PRIORITY = Thread.NORM_PRIORITY;
	public static final int DEFAULT_RCVR_PRIORITY = Thread.NORM_PRIORITY;
	// Default close timeout is 2 seconds
	public static final long DEFAULT_CLOSE_TIMEOUT = 2000;
	// Default maximum cached messages on object stream is 50
	public static final int DEFAULT_MAX_BUFFER_MSG = 50;
	public static final int DEFAULT_WAIT_INTERVAL = 10;
	protected Socket socket;
	private String addressPort = "-1:<no endpoint>:-1";
	// Underlying streams
	protected ObjectOutputStream outputStream;
	protected ObjectInputStream inputStream;
	// Event handler
	protected ISynchAsynchEventHandler handler;
	// Our queue
	protected SimpleQueueImpl queue = new SimpleQueueImpl();
	protected int keepAlive = 0;
	protected Thread sendThread;
	protected Thread rcvThread;
	protected Thread keepAliveThread;
	protected boolean isClosing = false;
	protected boolean waitForPing = false;
	protected PingMessage ping = new PingMessage();
	protected PingResponseMessage pingResp = new PingResponseMessage();
	protected int maxMsg = DEFAULT_MAX_BUFFER_MSG;
	protected long closeTimeout = DEFAULT_CLOSE_TIMEOUT;
	protected Vector eventNotify = null;
	protected Map properties;
	protected ID containerID = null;
	protected Object pingLock = new Object();
	private boolean disconnectHandled = false;
	private Object disconnectLock = new Object();
	
	public Client(Socket aSocket, ObjectInputStream iStream,
			ObjectOutputStream oStream,
			ISynchAsynchEventHandler handler, int keepAlive)
			throws IOException {
		this(aSocket, iStream, oStream, handler, keepAlive,
				DEFAULT_MAX_BUFFER_MSG);
	}
	private void setSocket(Socket s) throws SocketException {
		socket = s;
		if (s != null)
			addressPort = s.getLocalPort() + ":"
					+ s.getInetAddress().getHostName() + ":" + s.getPort();
		else
			addressPort = "-1:<no endpoint>:-1";
	}
	public Client(Socket aSocket, ObjectInputStream iStream,
			ObjectOutputStream oStream,
			ISynchAsynchEventHandler handler, int keepAlive,
			int maxmsgs) throws IOException {
		if (handler == null)
			throw new NullPointerException("event handler cannot be null");
		this.keepAlive = keepAlive;
		setSocket(aSocket);
		inputStream = iStream;
		outputStream = oStream;
		this.handler = handler;
		containerID = handler.getEventHandlerID();
		maxMsg = maxmsgs;
		properties = new Properties();
		setupThreads();
	}
	public Client(ISynchAsynchEventHandler handler, Integer keepAlive) {
		this(handler, keepAlive.intValue());
	}
	public Client(ISynchAsynchEventHandler handler, int keepAlive) {
		this(handler, keepAlive, DEFAULT_MAX_BUFFER_MSG);
	}
	public Client(ISynchAsynchEventHandler handler, int keepAlive,
			int maxmsgs) {
		if (handler == null)
			throw new NullPointerException("event handler cannot be null");
		this.handler = handler;
		containerID = handler.getEventHandlerID();
		this.keepAlive = keepAlive;
		maxMsg = maxmsgs;
		this.properties = new Properties();
	}
	public synchronized ID getLocalID() {
		if (containerID != null)
			return containerID;
		if (socket == null)
			return null;
		ID retID = null;
		try {
			retID = IDFactory.getDefault().createStringID(
					PROTOCOL + "://" + socket.getLocalAddress().getHostName()
							+ ":" + socket.getLocalPort());
		} catch (Exception e) {
			dumpStack("Exception in getLocalID()", e);
			return null;
		}
		return retID;
	}
	public synchronized void removeListener(IConnectionListener l) {
		eventNotify.remove(l);
	}
	public synchronized void addListener(IConnectionListener l) {
		if (eventNotify == null)
			eventNotify = new Vector();
		eventNotify.add(l);
	}
	public synchronized boolean isConnected() {
		if (socket != null)
			return socket.isConnected();
		else
			return false;
	}
	public synchronized boolean isStarted() {
		if (sendThread != null)
			return sendThread.isAlive();
		else
			return false;
	}
	private void setSocketOptions(Socket aSocket) throws SocketException {
		aSocket.setTcpNoDelay(true);
		if (keepAlive > 0) {
			aSocket.setKeepAlive(true);
			aSocket.setSoTimeout(keepAlive);
		}
	}
	public synchronized Object connect(ID remote, Object data, int timeout)
			throws IOException {
		trace("connect(" + remote + "," + data + "," + timeout + ")");
		if (socket != null)
			throw new ConnectException("Already connected to "
					+ getAddressPort());
		// parse URI
		URI anURI = null;
		try {
			anURI = new URI(remote.getName());
		} catch (URISyntaxException e) {
			IOException except = new IOException("Invalid URI for remote "
					+ remote);
			except.setStackTrace(e.getStackTrace());
			throw except;
		}
		// Get socket factory and create/connect socket
		SocketFactory fact = SocketFactory.getSocketFactory();
		if (fact == null)
			fact = SocketFactory.getDefaultSocketFactory();
		Socket s = fact.createSocket(anURI.getHost(), anURI.getPort(), timeout);
		// Set socket options
		setSocketOptions(s);
		// Now we've got a connection so set our socket
		setSocket(s);
		outputStream = new ObjectOutputStream(s.getOutputStream());
		outputStream.flush();
		inputStream = new ObjectInputStream(s.getInputStream());
		trace("connect;" + anURI);
		// send connect data and get syncronous response
		send(new ConnectRequestMessage(anURI, (Serializable) data));
		ConnectResultMessage res = null;
		res = (ConnectResultMessage) readObject();
		trace("connect;rcv:" + res);
		// Setup threads
		setupThreads();
		// Return results.
		Object ret = res.getData();
		trace("connect;returning:" + ret);
		return ret;
	}
	private void setupThreads() {
		// Setup threads
		trace("setupThreads()");
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
				// Loop until done sending messages (thread explicitly
				// interrupted or queue.peekQueue() returns null
				for (;;) {
					if (me.isInterrupted())
						break;
					// sender should wait here until something appears in queue
					// or queue is stopped (returns null)
					Serializable aMsg = (Serializable) queue.peekQueue();
					if (me.isInterrupted() || aMsg == null)
						break;
					try {
						// Actually send message
						send(aMsg);
						// Successful...remove message from queue
						queue.removeHead();
						if (msgCount >= maxMsg) {
							outputStream.reset();
							msgCount = 0;
						} else
							msgCount++;
					} catch (Exception e) {
						handleException(e);
						break;
					}
				}
				handleException(null);
				trace("SENDER TERMINATING");
			}
		}, getLocalID() + ":sndr:" + getAddressPort());
		// Set priority for new thread
		aThread.setPriority(DEFAULT_SNDR_PRIORITY);
		return aThread;
	}
	private void handleException(Throwable e) {
		synchronized (disconnectLock) {
			if (!disconnectHandled) {
				disconnectHandled = true;
				if (e != null)
					dumpStack("handleException in thread="
							+ Thread.currentThread().getName(), e);
					handler
							.handleDisconnectEvent(new DisconnectEvent(
									this, e, queue));
			}
		}
		synchronized (Client.this) {
			Client.this.notifyAll();
		}
	}
	private void closeSocket() {
		try {
			if (socket != null) {
				socket.close();
				setSocket(null);
			}
		} catch (IOException e) {
			dumpStack("closeSocket Exception", e);
		}
	}
	private void send(Serializable snd) throws IOException {
		trace("send(" + snd + ")");
		outputStream.writeObject(snd);
		outputStream.flush();
	}
	private void handlePingResp() {
		synchronized (pingLock) {
			waitForPing = false;
		}
	}
	public void setCloseTimeout(long t) {
		closeTimeout = t;
	}
	private void sendClose(Serializable snd) throws IOException {
		isClosing = true;
		trace("sendClose(" + snd + ")");
		send(snd);
		int count = 0;
		int interval = DEFAULT_WAIT_INTERVAL;
		while (!disconnectHandled && count < interval) {
			try {
				wait(closeTimeout / interval);
				count++;
			} catch (InterruptedException e) {
				dumpStack("sendClose wait", e);
				return;
			}
		}
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
					} catch (Exception e) {
						handleException(e);
						break;
					}
				}
				handleException(null);
				trace("RCVR TERMINATING");
			}
		}, getLocalID() + ":rcvr:" + getAddressPort());
		// Set priority and return
		aThread.setPriority(DEFAULT_RCVR_PRIORITY);
		return aThread;
	}
	// private int rcvCount = 0;
	private void handleRcv(Serializable rcv) throws IOException {
		try {
			trace("recv(" + rcv + ")");
			// Handle all messages
			if (rcv instanceof SynchMessage) {
				// Handle synch message. The only valid synch message is
				// 'close'.
				handler.handleSynchEvent(new SynchEvent(this,
						((SynchMessage) rcv).getData()));
			} else if (rcv instanceof AsynchMessage) {
				Serializable d = ((AsynchMessage) rcv).getData();
				// Handle asynch messages.
				handler.handleAsynchEvent(new AsynchEvent(this, d));
			} else if (rcv instanceof PingMessage) {
				// Handle ping by sending response back immediately
				send(pingResp);
			} else if (rcv instanceof PingResponseMessage) {
				// Handle ping response
				handlePingResp();
			} else
				throw new IOException("Invalid message received.");
		} catch (IOException e) {
			disconnect();
			throw e;
		}
	}
	public synchronized void start() {
		trace("start()");
		if (sendThread != null)
			sendThread.start();
		if (rcvThread != null)
			rcvThread.start();
		// Setup and start keep alive thread
		if (keepAlive > 0)
			keepAliveThread = setupPing();
		if (keepAliveThread != null)
			keepAliveThread.start();
	}
	public void stop() {
		trace("stop()");
	}
	private Thread setupPing() {
		trace("setupPing()");
		final int pingStartWait = (new Random()).nextInt(keepAlive / 2);
		return new Thread(new Runnable() {
			public void run() {
				Thread me = Thread.currentThread();
				// Sleep a random interval to start
				try {
					Thread.sleep(pingStartWait);
				} catch (InterruptedException e) {
					return;
				}
				// Setup ping frequency as keepAlive /2
				int frequency = keepAlive / 2;
				while (!queue.isStopped()) {
					try {
						// We give up if thread interrupted or disconnect has
						// occurred
						if (me.isInterrupted() || disconnectHandled)
							break;
						// Sleep for timeout interval divided by two
						Thread.sleep(frequency);
						// We give up if thread interrupted or disconnect has
						// occurred
						if (me.isInterrupted() || disconnectHandled)
							break;
						synchronized (pingLock) {
							waitForPing = true;
							// Actually queue ping instance for send by sender
							// thread
							queue.enqueue(ping);
							// send(ping);
							int count = 0;
							int interval = DEFAULT_WAIT_INTERVAL;
							while (waitForPing && count < interval) {
								pingLock.wait(frequency / interval);
								count++;
							}
							// If we haven't received a response, then we assume
							// the remote is not reachable and throw
							if (waitForPing)
								throw new IOException(getAddressPort()
										+ " remote not reachable with ping");
						}
					} catch (Exception e) {
						handleException(e);
						break;
					}
				}
				handleException(null);
				trace("PING TERMINATING");
			}
		}, getLocalID() + ":ping:" + getAddressPort());
	}
	public synchronized void disconnect() throws IOException {
		trace("disconnect()");
		// Close send queue and socket
		queue.close();
		closeSocket();
		if (keepAliveThread != null) {
			if (Thread.currentThread() != keepAliveThread)
				keepAliveThread.interrupt();
			keepAliveThread = null;
		}
		if (sendThread != null) {
			sendThread = null;
		}
		if (rcvThread != null) {
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
		trace("queueObject(" + recipient + "," + obj + ")");
		queue.enqueue(new AsynchMessage(obj));
	}
	public synchronized Serializable sendObject(ID recipient, Serializable obj)
			throws IOException {
		if (queue.isStopped() || isClosing)
			throw new ConnectException("Not connected");
		trace("sendObject(" + recipient + "," + obj + ")");
		sendClose(new SynchMessage(obj));
		return null;
	}
	public Object sendSynch(ID rec, Object obj) throws IOException {
		trace("sendSynch(" + rec + "," + obj + ")");
		return sendObject(rec, (Serializable) obj);
	}
	public Object sendSynch(ID rec, byte[] obj) throws IOException {
		trace("sendSynch(" + rec + "," + obj + ")");
		return sendObject(rec, obj);
	}
	private Serializable readObject() throws IOException {
		Serializable ret = null;
		try {
			ret = (Serializable) inputStream.readObject();
		} catch (ClassNotFoundException e) {
			dumpStack("readObject;classnotfoundexception", e);
			IOException except = new IOException(
					"Protocol violation due to class load failure.  "
							+ e.getMessage());
			except.setStackTrace(e.getStackTrace());
			throw except;
		}
		return ret;
	}
	public Map getProperties() {
		return properties;
	}
	public Object getAdapter(Class clazz) {
		return null;
	}
	private String getAddressPort() {
		return addressPort;
	}
	protected void trace(String msg) {
		if (Trace.ON && trace != null) {
			trace.msg(getLocalID() + ":" + getAddressPort() + ";" + msg);
		}
	}
	protected void dumpStack(String msg, Throwable e) {
		if (Trace.ON && trace != null) {
			trace.dumpStack(e, getLocalID() + ":" + getAddressPort() + ";"
					+ msg);
		}
	}
	public void setProperties(Map props) {
		this.properties = props;
	}
}