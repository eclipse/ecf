/****************************************************************************
 * Copyright (c) 2006, 2008 Remy Suen, Composent Inc., and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.protocol.bittorrent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * The <code>TorrentServer</code> class listens for incoming connections and
 * hooks them onto the corresponding {@link Torrent} based on the info hash
 * provided in the hand shake.
 */
public class TorrentServer {

	private static final Map activeTorrents = new HashMap();

	/**
	 * The shared instance of a <code>TorrentServer</code> that is currently
	 * monitoring a port for incoming connections.
	 */
	private static TorrentServer peerListener;

	/**
	 * The port to use to listen for incoming connections.
	 */
	private static int port = -1;

	/**
	 * Used to read and process an incoming connection's handshake.
	 */
	private final ByteBuffer buffer = ByteBuffer.allocate(68);

	private final byte[] bufferArray = buffer.array();

	private final byte[] handshake = new byte[20];

	private ServerSocketChannel channel;

	private ServerSocket serverSocket;

	private Thread listeningThread;

	static {
		try {
			peerListener = new TorrentServer(null);
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}

	/**
	 * Sets the port that should be used to listen for incoming connections.
	 * 
	 * @param port
	 *            the port to listen on
	 * @throws IllegalArgumentException
	 *             If <code>port</code> is negative
	 * @throws IOException
	 *             If an I/O error occurs while binding around the specified
	 *             port
	 */
	public static void setPort(int port) throws IOException {
		if (port < 0) {
			throw new IllegalArgumentException("Cannot listen for incoming connections on a negative port: " + port); //$NON-NLS-1$
		}

		if (peerListener == null || TorrentServer.port != port) {
			try {
				peerListener.channel.close();
			} catch (IOException e) {
				// ignored
			}
			peerListener = new TorrentServer(port);
			peerListener.listen();
		}
	}

	/**
	 * Retrieves the port that is currently being used to listen for incoming
	 * connections.
	 * 
	 * @return the port being used
	 */
	public static int getPort() {
		return port;
	}

	static void addTorrent(String hash, Torrent torrent) {
		if (!activeTorrents.containsKey(hash)) {
			activeTorrents.put(hash, torrent);
			if (activeTorrents.size() == 1) {
				peerListener.listen();
			}
		}
	}

	static Torrent get(String hash) {
		return (Torrent) activeTorrents.get(hash);
	}

	static Torrent remove(String hash) {
		Torrent host = (Torrent) activeTorrents.remove(hash);
		if (activeTorrents.isEmpty()) {
			try {
				peerListener.channel.close();
			} catch (IOException e) {
				// ignored
			}
			peerListener.listeningThread = null;
		}
		return host;
	}

	private TorrentServer(InetSocketAddress address) throws IOException {
		channel = ServerSocketChannel.open();
		serverSocket = channel.socket();
		serverSocket.bind(address);
		port = serverSocket.getLocalPort();
	}

	private TorrentServer(int port) throws IOException {
		this(new InetSocketAddress("localhost", port)); //$NON-NLS-1$
	}

	/**
	 * Starts a new thread and begins listening for incoming connections.
	 */
	private void listen() {
		if (listeningThread == null) {
			listeningThread = new ListeningThread();
			listeningThread.start();
		}
	}

	private class ListeningThread extends Thread {

		public ListeningThread() {
			super("Listening Thread"); //$NON-NLS-1$
		}

		public void run() {
			int read = 0;
			int ret = 0;
			while (true) {
				try {
					SocketChannel socketChannel = channel.accept();
					ret = socketChannel.read(buffer);
					if (ret == -1) {
						try {
							socketChannel.close();
						} catch (IOException e) {
							// ignored
						}
						continue;
					}
					read += ret;
					while (read < 68) {
						ret = socketChannel.read(buffer);
						if (ret == -1) {
							try {
								socketChannel.close();
							} catch (IOException e) {
								// ignored
							}
							break;
						}
						read += ret;
					}
					if (ret == -1) {
						continue;
					}
					System.arraycopy(bufferArray, 28, handshake, 0, 20);
					Torrent torrent = (Torrent) activeTorrents.get(new String(
							handshake, "ISO-8859-1")); //$NON-NLS-1$
					if (torrent != null) {
						torrent.connectTo(socketChannel);
					} else {
						// since no such torrent is currently online and
						// connected, simply close the connection
						try {
							socketChannel.close();
						} catch (IOException e) {
							// ignored
						}
					}
					buffer.clear();
				} catch (AsynchronousCloseException e) {
					return;
				} catch (ClosedChannelException e) {
					return;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

}
