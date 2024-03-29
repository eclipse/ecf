/****************************************************************************
 * Copyright (c) 2012 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.provider.comm.tcp;

import java.io.*;
import java.net.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.SecureRandom;
import java.util.*;
import javax.net.ssl.SSLSocketFactory;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.sharedobject.util.SimpleFIFOQueue;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.provider.ECFProviderDebugOptions;
import org.eclipse.ecf.internal.provider.ProviderPlugin;
import org.eclipse.ecf.provider.comm.*;

/**
 * @since 4.3
 */
public final class SSLClient implements ISynchAsynchConnection {
	public static final String PROTOCOL = "ecfssl"; //$NON-NLS-1$
	public static final int DEFAULT_SNDR_PRIORITY = Thread.NORM_PRIORITY;
	public static final int DEFAULT_RCVR_PRIORITY = Thread.NORM_PRIORITY;
	// Default close timeout is 2 seconds
	public static final long DEFAULT_CLOSE_TIMEOUT = 2000;
	// Default maximum cached messages on object stream is 50
	public static final int DEFAULT_MAX_BUFFER_MSG = 50;
	public static final int DEFAULT_WAIT_INTERVAL = 10;
	protected Socket socket;
	private String addressPort = "-1:<no endpoint>:-1"; //$NON-NLS-1$
	// Underlying streams
	protected ObjectOutputStream outputStream;
	protected ObjectInputStream inputStream;
	// Event handler
	protected ISynchAsynchEventHandler handler;
	// Our queue
	protected SimpleFIFOQueue queue = new SimpleFIFOQueue();
	protected int keepAlive = 0;
	protected Thread sendThread;
	protected Thread rcvThread;
	protected Thread keepAliveThread;
	protected boolean isClosing = false;
	protected boolean waitForPing = false;
	protected PingMessage ping = new PingMessage();
	protected PingResponseMessage pingResp = new PingResponseMessage();
	protected long closeTimeout = DEFAULT_CLOSE_TIMEOUT;
	protected Map properties;
	protected ID containerID = null;
	protected Object pingLock = new Object();
	boolean disconnectHandled = false;
	private final Object disconnectLock = new Object();
	protected final Object outputStreamLock = new Object();
	private int maxmsgs = DEFAULT_MAX_BUFFER_MSG;

	private String getHostNameForAddressWithoutLookup(InetAddress inetAddress) {
		// First get InetAddress.toString(), which returns
		// the inet address in this form:  "hostName/address".
		// If hostname is not resolved the result is: "/address"
		// So first we detect the location of the "/" to determine
		// whether the host name is there or not
		String inetAddressStr = inetAddress.toString();
		int slashPos = inetAddressStr.indexOf('/');
		if (slashPos == 0)
			// no hostname is available so we strip
			// off '/' and return address as string
			return inetAddressStr.substring(1);

		// hostname is there/non-null, so we use it
		return inetAddressStr.substring(0, slashPos);

	}

	/**
	 * @param s
	 * @throws SocketException not thrown by this implementation.
	 */
	private void setSocket(Socket s) throws SocketException {
		socket = s;
		if (s != null)
			addressPort = s.getLocalPort() + ":" //$NON-NLS-1$
					+ getHostNameForAddressWithoutLookup(s.getInetAddress()) + ":" + s.getPort(); //$NON-NLS-1$
		else
			addressPort = "-1:<no endpoint>:-1"; //$NON-NLS-1$
	}

	public SSLClient(Socket aSocket, ObjectInputStream iStream, ObjectOutputStream oStream, ISynchAsynchEventHandler handler) throws IOException {
		this(aSocket, iStream, oStream, handler, DEFAULT_MAX_BUFFER_MSG);
	}

	public SSLClient(Socket aSocket, ObjectInputStream iStream, ObjectOutputStream oStream, ISynchAsynchEventHandler handler, int maxmsgs) throws IOException {
		Assert.isNotNull(aSocket);
		if (aSocket.getKeepAlive())
			keepAlive = aSocket.getSoTimeout();
		setSocket(aSocket);
		inputStream = iStream;
		outputStream = oStream;
		this.handler = handler;
		containerID = handler.getEventHandlerID();
		properties = new Properties();
		setupThreads();
	}

	public SSLClient(ISynchAsynchEventHandler handler, int keepAlive) {
		if (handler == null)
			throw new NullPointerException("event handler cannot be null"); //$NON-NLS-1$
		this.handler = handler;
		this.keepAlive = keepAlive;
		containerID = handler.getEventHandlerID();
		this.properties = new HashMap();
	}

	public synchronized ID getLocalID() {
		if (containerID != null)
			return containerID;
		if (socket == null)
			return null;
		ID retID = null;
		try {
			retID = IDFactory.getDefault().createStringID(PROTOCOL + "://" + getHostNameForAddressWithoutLookup(socket.getLocalAddress()) //$NON-NLS-1$
					+ ":" + socket.getLocalPort()); //$NON-NLS-1$
		} catch (final Exception e) {
			traceStack("Exception in getLocalID()", e); //$NON-NLS-1$
			return null;
		}
		return retID;
	}

	public void removeListener(IConnectionListener l) {
		// XXX does not support listeners
	}

	public void addListener(IConnectionListener l) {
		// XXX does not support listeners
	}

	public synchronized boolean isConnected() {
		if (socket != null)
			return socket.isConnected();
		return false;
	}

	public synchronized boolean isStarted() {
		if (sendThread != null)
			return sendThread.isAlive();
		return false;
	}

	private void setSocketOptions(Socket aSocket) throws SocketException {
		aSocket.setTcpNoDelay(true);
		if (keepAlive > 0) {
			aSocket.setKeepAlive(true);
			aSocket.setSoTimeout(keepAlive);
		}
	}

	private Socket createSocket(String host, int port, int timeout) throws IOException {
		SSLSocketFactory socketFactory = ProviderPlugin.getDefault().getSSLSocketFactory();
		if (socketFactory == null)
			throw new IOException("Cannot get SSLSocketFactory to create SSLSocket for host=" + host + ",port=" + port); //$NON-NLS-1$ //$NON-NLS-2$
		Socket s = socketFactory.createSocket();
		s.connect(new InetSocketAddress(host, port), timeout);
		return s;
	}

	public synchronized Object connect(ID remote, Object data, int timeout) throws ECFException {
		debug("connect(" + remote + "," + data + "," + timeout + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		if (socket != null)
			throw new ECFException("Already connected"); //$NON-NLS-1$
		// parse URI
		URI anURI = null;
		try {
			anURI = new URI(remote.getName());
		} catch (final URISyntaxException e) {
			throw new ECFException("Invalid URI for remoteID=" + remote, e); //$NON-NLS-1$
		}
		ConnectResultMessage res = null;
		try {
			final Socket s = createSocket(anURI.getHost(), anURI.getPort(), timeout);
			// Set socket options
			setSocketOptions(s);
			// Now we've got a connection so set our socket
			setSocket(s);
			outputStream = new ObjectOutputStream(s.getOutputStream());
			outputStream.flush();
			inputStream = ProviderPlugin.getDefault().createObjectInputStream(s.getInputStream());
			debug("connect;" + anURI); //$NON-NLS-1$
			// send connect data and get synchronous response
			send(new ConnectRequestMessage(anURI, (Serializable) data));
			res = (ConnectResultMessage) readObject();
		} catch (final Exception e) {
			throw new ECFException("Exception during connection to " + remote.getName(), e); //$NON-NLS-1$
		}
		debug("connect;rcv:" + res); //$NON-NLS-1$
		// Setup threads
		setupThreads();
		// Return results.
		final Object ret = res.getData();
		debug("connect;returning:" + ret); //$NON-NLS-1$
		return ret;
	}

	@SuppressWarnings("unchecked")
	private void setupThreads() {
		// Setup threads
		debug("setupThreads()"); //$NON-NLS-1$
		sendThread = (Thread) AccessController.doPrivileged(new PrivilegedAction() {
			public Object run() {
				return getSendThread();
			}
		});
		rcvThread = (Thread) AccessController.doPrivileged(new PrivilegedAction() {
			public Object run() {
				return getRcvThread();
			}
		});
	}

	Thread getSendThread() {
		final Thread aThread = new Thread(new Runnable() {
			public void run() {
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
					} catch (Exception e) {
						handleException(e);
						break;
					}
				}
				handleException(null);
				debug("SENDER TERMINATING"); //$NON-NLS-1$
			}
		}, getLocalID() + ":sndr:" + getAddressPort()); //$NON-NLS-1$
		// Set priority for new thread
		aThread.setPriority(DEFAULT_SNDR_PRIORITY);
		return aThread;
	}

	void handleException(Throwable e) {
		synchronized (disconnectLock) {
			if (!disconnectHandled) {
				disconnectHandled = true;
				if (e != null)
					traceStack("handleException in thread=" //$NON-NLS-1$
							+ Thread.currentThread().getName(), e);
				handler.handleDisconnectEvent(new DisconnectEvent(this, e, queue));
			}
		}
		synchronized (SSLClient.this) {
			SSLClient.this.notifyAll();
		}
	}

	private void closeSocket() {
		try {
			if (socket != null) {
				socket.close();
				setSocket(null);
			}
		} catch (final IOException e) {
			traceStack("closeSocket Exception", e); //$NON-NLS-1$
		}
	}

	private int resetCounter = 0;

	void send(Serializable snd) throws IOException {
		//		debug("send(" + snd + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		// need to synchronize to avoid concurrent access to outputStream
		synchronized (outputStreamLock) {
			outputStream.writeObject(snd);
			outputStream.flush();
			if (resetCounter > this.maxmsgs) {
				outputStream.reset();
				resetCounter = 0;
			} else
				resetCounter++;
		}
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
		debug("sendClose(" + snd + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		send(snd);
		int count = 0;
		final int interval = DEFAULT_WAIT_INTERVAL;
		while (!disconnectHandled && count < interval) {
			try {
				wait(closeTimeout / interval);
				count++;
			} catch (final InterruptedException e) {
				traceStack("sendClose wait", e); //$NON-NLS-1$
				return;
			}
		}
	}

	Thread getRcvThread() {
		final Thread aThread = new Thread(new Runnable() {
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
				debug("RCVR TERMINATING"); //$NON-NLS-1$
			}
		}, getLocalID() + ":rcvr:" + getAddressPort()); //$NON-NLS-1$
		// Set priority and return
		aThread.setPriority(DEFAULT_RCVR_PRIORITY);
		return aThread;
	}

	// private int rcvCount = 0;
	void handleRcv(Serializable rcv) throws IOException {
		try {
			//			debug("recv(" + rcv + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			// Handle all messages
			if (rcv instanceof SynchMessage) {
				// Handle synch message. The only valid synch message is
				// 'close'.
				handler.handleSynchEvent(new SynchEvent(this, ((SynchMessage) rcv).getData()));
			} else if (rcv instanceof AsynchMessage) {
				final Serializable d = ((AsynchMessage) rcv).getData();
				// Handle asynch messages.
				handler.handleAsynchEvent(new AsynchEvent(this, d));
			} else if (rcv instanceof PingMessage) {
				// Handle ping by sending response back immediately
				send(pingResp);
			} else if (rcv instanceof PingResponseMessage) {
				// Handle ping response
				handlePingResp();
			} else
				throw new IOException("Invalid message received"); //$NON-NLS-1$
		} catch (final IOException e) {
			disconnect();
			throw e;
		}
	}

	public synchronized void start() {
		debug("start()"); //$NON-NLS-1$
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
		debug("stop()"); //$NON-NLS-1$
	}

	private Thread setupPing() {
		debug("setupPing()"); //$NON-NLS-1$
		final int pingStartWait = (new SecureRandom()).nextInt(keepAlive / 2);
		return new Thread(new Runnable() {
			public void run() {
				final Thread me = Thread.currentThread();
				// Sleep a random interval to start
				try {
					Thread.sleep(pingStartWait);
				} catch (final InterruptedException e) {
					return;
				}
				// Setup ping frequency as keepAlive /2
				final int frequency = keepAlive / 2;
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
							final int interval = DEFAULT_WAIT_INTERVAL;
							while (waitForPing && count < interval) {
								pingLock.wait(frequency / interval);
								count++;
							}
							// If we haven't received a response, then we assume
							// the remote is not reachable and throw
							if (waitForPing)
								throw new IOException(getAddressPort() + " remote not reachable by ping"); //$NON-NLS-1$
						}
					} catch (final Exception e) {
						handleException(e);
						break;
					}
				}
				handleException(null);
				debug("PING TERMINATING"); //$NON-NLS-1$
			}
		}, getLocalID() + ":ping:" + getAddressPort()); //$NON-NLS-1$
	}

	public synchronized void disconnect() {
		debug("disconnect()"); //$NON-NLS-1$
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

	public synchronized void queueObject(ID recipient, Serializable obj) throws IOException {
		if (queue.isStopped() || isClosing)
			throw new ConnectException("Not connected"); //$NON-NLS-1$
		queue.enqueue(new AsynchMessage(obj));
	}

	public synchronized Serializable sendObject(ID recipient, Serializable obj) throws IOException {
		if (queue.isStopped() || isClosing)
			throw new ConnectException("Not connected"); //$NON-NLS-1$
		sendClose(new SynchMessage(obj));
		return null;
	}

	public Object sendSynch(ID rec, Object obj) throws IOException {
		return sendObject(rec, (Serializable) obj);
	}

	public Object sendSynch(ID rec, byte[] obj) throws IOException {
		return sendObject(rec, obj);
	}

	Serializable readObject() throws IOException {
		Serializable ret = null;
		try {
			ret = (Serializable) inputStream.readObject();
		} catch (final ClassNotFoundException e) {
			traceStack("readObject;classnotfoundexception", e); //$NON-NLS-1$
			final IOException except = new IOException("Protocol violation due to class load failure"); //$NON-NLS-1$
			except.setStackTrace(e.getStackTrace());
			throw except;
		}
		return ret;
	}

	public Map getProperties() {
		return properties;
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class clazz) {
		return null;
	}

	String getAddressPort() {
		return addressPort;
	}

	protected void debug(String msg) {
		Trace.trace(ProviderPlugin.PLUGIN_ID, ECFProviderDebugOptions.CONNECTION, getLocalID() + "." + msg); //$NON-NLS-1$
	}

	protected void traceStack(String msg, Throwable e) {
		Trace.catching(ProviderPlugin.PLUGIN_ID, ECFProviderDebugOptions.EXCEPTIONS_CATCHING, SSLClient.class, msg, e);
	}

	public void setProperties(Map props) {
		this.properties = props;
	}
}