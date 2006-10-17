package org.jivesoftware.smackx.nat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import org.jivesoftware.smack.XMPPException;
import org.xmlpull.v1.*;
import org.xmlpull.mxp1.MXParser;

import java.net.URL;

import de.javawi.jstun.test.BindingLifetimeTest;
import de.javawi.jstun.test.DiscoveryInfo;
import de.javawi.jstun.test.DiscoveryTest;

/**
 * Transport resolver using the STUN library.
 * 
 * @author Alvaro Saurin
 */
public class STUNResolver extends TransportResolver {

	// The filename where the STUN servers are stored.
	public final static String STUNSERVERS_FILENAME = "META-INF/stun-config.xml";

	// Fallback values when we don't have any STUN server to use...
	private final static String FALLBACKHOSTNAME = "stun.xten.net";

	private final static int FALLBACKHOSTPORT = 3478;

	// Current STUN server we are using
	private STUNService currentServer;

	private Thread resolverThread;

	private int defaultPort;

	/**
	 * Constructor with default STUN server.
	 */
	public STUNResolver() {
		super();

		this.defaultPort = 0;
		this.currentServer = new STUNService();
	}

	/**
	 * Constructor with a default port.
	 * 
	 * @param defaultPort Port to use by default.
	 */
	public STUNResolver(int defaultPort) {
		this();

		this.defaultPort = defaultPort;
	}

	/**
	 * Return true if the service is working.
	 * 
	 * @see org.jivesoftware.smackx.nat.TransportResolver#isResolving()
	 */
	public boolean isResolving() {
		return super.isResolving() && resolverThread != null;
	}

	/**
	 * Set the STUN server name and port
	 * 
	 * @param ip the STUN server name
	 * @param port the STUN server port
	 */
	public void setSTUNService(final String ip, final int port) {
		currentServer = new STUNService(ip, port);
	}

	/**
	 * Get the name of the current STUN server.
	 * 
	 * @return the name of the STUN server
	 */
	public String getCurrentServerName() {
		if (!currentServer.isNull()) {
			return currentServer.getHostname();
		} else {
			return null;
		}
	}

	/**
	 * Get the port of the current STUN server.
	 * 
	 * @return the port of the STUN server
	 */
	public int getCurrentServerPort() {
		if (!currentServer.isNull()) {
			return currentServer.getPort();
		} else {
			return 0;
		}
	}

	/**
	 * Load the STUN configuration from a stream.
	 * 
	 * @param stunConfigStream An InputStream with the configuration file.
	 * @return A list of loaded servers
	 */
	public ArrayList loadSTUNServers(final java.io.InputStream stunConfigStream) {
		ArrayList serversList = new ArrayList();
		String serverName;
		int serverPort;

		try {
			XmlPullParser parser = new MXParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
			parser.setInput(stunConfigStream, "UTF-8");

			int eventType = parser.getEventType();
			do {
				if (eventType == XmlPullParser.START_TAG) {

					// Parse a STUN server definition
					if (parser.getName().equals("stunServer")) {

						serverName = null;
						serverPort = -1;

						// Parse the hostname
						parser.next();
						parser.next();
						serverName = parser.nextText();

						// Parse the port
						parser.next();
						parser.next();
						try {
							serverPort = Integer.parseInt(parser.nextText());
						} catch (Exception e) {
						}

						// If we have a valid hostname and port, add
						// it to the list.
						if (serverName != null && serverPort != -1) {
							STUNService service = new STUNService(serverName, serverPort);

							serversList.add(service);
						}
					}
				}
				eventType = parser.next();

			} while (eventType != XmlPullParser.END_DOCUMENT);

		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		currentServer = bestSTUNServer(serversList);

		return serversList;
	}

	/**
	 * Load a list of services: STUN servers and ports. Some public STUN servers
	 * are:
	 * 
	 * <pre>
	 *               iphone-stun.freenet.de:3478
	 *               larry.gloo.net:3478
	 *               stun.xten.net:3478
	 *               stun.fwdnet.net
	 *               stun.fwd.org (no DNS SRV record)
	 *               stun01.sipphone.com (no DNS SRV record)
	 *               stun.softjoys.com (no DNS SRV record)
	 *               stun.voipbuster.com (no DNS SRV record)
	 *               stun.voxgratia.org (no DNS SRV record)
	 *               stun.noc.ams-ix.net
	 * </pre>
	 * 
	 * This list should be contained in a file in the "META-INF" directory
	 * 
	 * @return a list of services
	 */
	public ArrayList loadSTUNServers() {
		ArrayList serversList = new ArrayList();

		// Load the STUN configuration
		try {
			// Get an array of class loaders to try loading the config from.
			ClassLoader[] classLoaders = new ClassLoader[2];
			classLoaders[0] = new STUNResolver().getClass().getClassLoader();
			classLoaders[1] = Thread.currentThread().getContextClassLoader();

			for (int i = 0; i < classLoaders.length; i++) {
				Enumeration stunConfigEnum = classLoaders[i]
						.getResources(STUNSERVERS_FILENAME);

				while (stunConfigEnum.hasMoreElements() && serversList.isEmpty()) {
					URL url = (URL) stunConfigEnum.nextElement();
					java.io.InputStream stunConfigStream = null;

					stunConfigStream = url.openStream();
					serversList.addAll(loadSTUNServers(stunConfigStream));
					stunConfigStream.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// If the list of candidates is empty, add at least one default server
		if (serversList.isEmpty()) {
			serversList.add(new STUNService(FALLBACKHOSTNAME, FALLBACKHOSTPORT));
		}

		return serversList;
	}

	/**
	 * Get the best usable STUN server from a list.
	 * 
	 * @return the best STUN server that can be used.
	 */
	private STUNService bestSTUNServer(final ArrayList listServers) {
		if (listServers.isEmpty()) {
			return null;
		} else {
			// TODO: this should use some more advanced criteria...
			return (STUNService) listServers.get(0);
		}
	}

	/**
	 * Obtain a free port we can use.
	 * 
	 * @return A free port number.
	 */
	private int getFreePort() {
		ServerSocket ss;
		int freePort = 0;
		try {
			ss = new ServerSocket(0);
			freePort = ss.getLocalPort();
			ss.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return freePort;
	}

	/**
	 * Resolve the IP and obtain a valid transport method.
	 */
	public synchronized void resolve() throws XMPPException {
		if (!isResolving()) {
			// Get the best STUN server available
			if (currentServer.isNull()) {
				loadSTUNServers();
			}

			// We should have a valid STUN server by now...
			if (!currentServer.isNull()) {

				setResolveInit();

				clearCandidates();

				resolverThread = new Thread(new Runnable() {
					public void run() {
						// Iterate through the list of interfaces, and ask
						// to the STUN server for our address.
						try {
							Enumeration ifaces = NetworkInterface.getNetworkInterfaces();
							String candAddress;
							int candPort;

							while (ifaces.hasMoreElements()) {

								NetworkInterface iface = (NetworkInterface) ifaces
										.nextElement();
								Enumeration iaddresses = iface.getInetAddresses();

								while (iaddresses.hasMoreElements()) {
									InetAddress iaddress = (InetAddress) iaddresses
											.nextElement();
									if (!iaddress.isLoopbackAddress()
											&& !iaddress.isLinkLocalAddress()) {

										// Reset the candidate
										candAddress = null;
										candPort = -1;

										DiscoveryTest test = new DiscoveryTest(iaddress,
												currentServer.getHostname(),
												currentServer.getPort());
										try {
											// Run the tests and get the
											// discovery
											// information, where all the
											// info is stored...
											DiscoveryInfo di = test.test();

											candAddress = di.getPublicIP()
													.getHostAddress();

											// Get a valid port
											if (defaultPort == 0) {
												candPort = getFreePort();
											} else {
												candPort = defaultPort;
											}

											// If we have a valid candidate,
											// add it to the list.
											if (candAddress != null && candPort >= 0) {
												addCandidate(new TransportCandidate.Fixed(
														candAddress, candPort));
											}
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}
							}
						} catch (SocketException e) {
							e.printStackTrace();
						} finally {
							setResolveEnd();
						}
					}
				}, "Waiting for all the transport candidates checks...");

				resolverThread.setName("STUN resolver");
				resolverThread.start();
			} else {
				throw new IllegalStateException("No valid STUN server found.");
			}
		}
	}

	/**
	 * Cancel any operation.
	 * 
	 * @see org.jivesoftware.smackx.nat.TransportResolver#cancel()
	 */
	public synchronized void cancel() throws XMPPException {
		if (isResolving()) {
			resolverThread.interrupt();
			setResolveEnd();
		}
	}

	/**
	 * Clear the list of candidates and start the resolution again.
	 * 
	 * @see org.jivesoftware.smackx.nat.TransportResolver#clear()
	 */
	public synchronized void clear() throws XMPPException {
		this.defaultPort = 0;
		super.clear();
	}

	/**
	 * STUN service definition.
	 */
	private class STUNService {

		private String hostname; // The hostname of the service

		private int port; // The port number

		/**
		 * Basic constructor, with the hostname and port
		 * 
		 * @param hostname The hostname
		 * @param port The port
		 */
		public STUNService(final String hostname, final int port) {
			super();

			this.hostname = hostname;
			this.port = port;
		}

		/**
		 * Default constructor, without name and port.
		 */
		public STUNService() {
			this(null, -1);
		}

		/**
		 * Get the host name of the STUN service.
		 * 
		 * @return The host name
		 */
		public String getHostname() {
			return hostname;
		}

		/**
		 * Set the hostname of the STUN service.
		 * 
		 * @param hostname The host name of the service.
		 */
		public void setHostname(final String hostname) {
			this.hostname = hostname;
		}

		/**
		 * Get the port of the STUN service
		 * 
		 * @return The port number where the STUN server is waiting.
		 */
		public int getPort() {
			return port;
		}

		/**
		 * Set the port number for the STUN service.
		 * 
		 * @param port The port number.
		 */
		public void setPort(final int port) {
			this.port = port;
		}

		/**
		 * Basic format test: the service is not null.
		 * 
		 * @return true if the hostname and port are null
		 */
		public boolean isNull() {
			if (hostname == null) {
				return true;
			} else if (hostname.length() == 0) {
				return true;
			} else if (port < 0) {
				return true;
			} else {
				return false;
			}
		}

		/**
		 * Check a binding with the STUN currentServer.
		 * 
		 * Note: this function blocks for some time, waiting for a response.
		 * 
		 * @return true if the currentServer is usable.
		 */
		public boolean checkBinding() {
			boolean result = false;

			try {
				BindingLifetimeTest binding = new BindingLifetimeTest(hostname, port);

				binding.test();

				while (true) {
					Thread.sleep(5000);
					if (binding.getLifetime() != -1) {
						if (binding.isCompleted()) {
							return true;
						}
					} else {
						break;
					}
				}
			} catch (Exception e) {
			}

			return result;
		}
	}
}
