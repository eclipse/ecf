/**
 * $RCSfile: JingleManager.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/10/17 19:12:42 $
 *
 * Copyright 2003-2005 Jive Software.
 *
 * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jivesoftware.smackx.jingle;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.ConnectionEstablishedListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.nat.TransportResolver;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.packet.Jingle;

/**
 * The JingleManager is a facade built upon Jabber Jingle (JEP-166) to allow the
 * use of the Jingle extension. This implementation allows the user to simply
 * use this class for setting the Jingle parameters.
 * 
 * </p>
 * 
 * This is an example of how to use the Jingle code:
 * 
 * <pre>
 * XMPPConnection con = new XMPPConnection(&quot;jabber.org&quot;);
 * 
 * TransportResolver tm = new STUNResolver();
 * 
 * JingleManager jmanager = new JingleManager(conn, tm);
 * 
 * // Insert the payloads in the &quot;mypayloads&quot; list...
 * 
 * OutgoingJingleSession jsession = jmanager.createOutgoingJingleSession(mypayloads);
 * 
 * // Install some session listeners...
 * 
 * </pre>
 * 
 * In order to use the Jingle extension, the user must provide a
 * TransportResolver that will handle the resolution of the external address of
 * the machine. This resolver can be initialized with several default resolvers,
 * including a fixed solver that can be used when the address and port are know
 * in advance. See TransportResolver or TransportManager for a complete list of
 * resolution services.
 * 
 * </p>
 * 
 * Before creating an outgoing connection, the user must create session
 * listeners that will be called when different events happen. The most
 * important event is <i>sessionEstablished()</i>, that will be called when all
 * the negotiations are finished, providing the payload type for the
 * transmission as well as the remote and local addresses and ports for the
 * communication. See JingleListener for a complete list of events that can be
 * observed.
 * 
 * </p>
 * 
 * @see JingleListener
 * @see TransportResolver
 * @see TransportManager
 * @see OutgoingJingleSession
 * @see IncomingJingleSession
 * 
 * </p>
 * @author Alvaro Saurin
 */
public class JingleManager {

	// non-static

	// Listeners for manager events (ie, session requests...)
	private List listeners;

	// The XMPP connection
	private XMPPConnection connection;

	// The Jingle transport manager
	private final TransportResolver resolver;

	static {
		ProviderManager.addIQProvider("jingle", "http://jabber.org/protocol/jingle",
									  new org.jivesoftware.smackx.provider.JingleProvider());
		
		ProviderManager.addExtensionProvider("description", "http://jabber.org/protocol/jingle/description/audio",
									  new org.jivesoftware.smackx.provider.JingleContentDescriptionProvider.Audio());
		
		ProviderManager.addExtensionProvider("transport", "http://jabber.org/protocol/jingle/transport/ice",
									  new org.jivesoftware.smackx.provider.JingleTransportProvider.Ice());
		ProviderManager.addExtensionProvider("transport", "http://jabber.org/protocol/jingle/transport/raw-udp",
									  new org.jivesoftware.smackx.provider.JingleTransportProvider.RawUdp());
		
		ProviderManager.addExtensionProvider("busy", "http://jabber.org/protocol/jingle/info/audio",
									  new org.jivesoftware.smackx.provider.JingleContentInfoProvider.Audio.Busy());
		ProviderManager.addExtensionProvider("hold", "http://jabber.org/protocol/jingle/info/audio",
									  new org.jivesoftware.smackx.provider.JingleContentInfoProvider.Audio.Hold());
		ProviderManager.addExtensionProvider("mute", "http://jabber.org/protocol/jingle/info/audio",
									  new org.jivesoftware.smackx.provider.JingleContentInfoProvider.Audio.Mute());
		ProviderManager.addExtensionProvider("queued", "http://jabber.org/protocol/jingle/info/audio",
									  new org.jivesoftware.smackx.provider.JingleContentInfoProvider.Audio.Queued());
		ProviderManager.addExtensionProvider("ringing", "http://jabber.org/protocol/jingle/info/audio",
									  new org.jivesoftware.smackx.provider.JingleContentInfoProvider.Audio.Ringing());

		
		// Enable the Jingle support on every established connection
		// The ServiceDiscoveryManager class should have been already
		// initialized
		XMPPConnection.addConnectionListener(new ConnectionEstablishedListener() {
			public void connectionEstablished(final XMPPConnection connection) {
				JingleManager.setServiceEnabled(connection, true);
			}
		});
	}

	/**
	 * Private constructor
	 */
	private JingleManager() {
		resolver = null;
	}

	/**
	 * Default constructor, with a connection.
	 * 
	 * @param conn
	 */
	public JingleManager(final XMPPConnection conn, final TransportResolver res) {
		connection = conn;
		resolver = res;
	}

	/**
	 * Enables or disables the Jingle support on a given connection.
	 * <p>
	 * 
	 * Before starting any Jingle media session, check that the user can handle
	 * it. Enable the Jingle support to indicate that this client handles Jingle
	 * messages.
	 * 
	 * @param connection the connection where the service will be enabled or
	 *            disabled
	 * @param enabled indicates if the service will be enabled or disabled
	 */
	public synchronized static void setServiceEnabled(final XMPPConnection connection,
			final boolean enabled) {
		if (isServiceEnabled(connection) == enabled) {
			return;
		}

		if (enabled) {
			ServiceDiscoveryManager.getInstanceFor(connection).addFeature(
					Jingle.NAMESPACE);
		} else {
			ServiceDiscoveryManager.getInstanceFor(connection).removeFeature(
					Jingle.NAMESPACE);
		}
	}

	/**
	 * Returns true if the Jingle support is enabled for the given connection.
	 * 
	 * @param connection the connection to look for Jingle support
	 * @return a boolean indicating if the Jingle support is enabled for the
	 *         given connection
	 */
	public static boolean isServiceEnabled(final XMPPConnection connection) {
		return ServiceDiscoveryManager.getInstanceFor(connection).includesFeature(
				Jingle.NAMESPACE);
	}

	/**
	 * Returns true if the specified user handles Jingle messages.
	 * 
	 * @param connection the connection to use to perform the service discovery
	 * @param userID the user to check. A fully qualified xmpp ID, e.g.
	 *            jdoe@example.com
	 * @return a boolean indicating whether the specified user handles Jingle
	 *         messages
	 */
	public static boolean isServiceEnabled(final XMPPConnection connection,
			final String userID) {
		try {
			DiscoverInfo result = ServiceDiscoveryManager.getInstanceFor(connection)
					.discoverInfo(userID);
			return result.containsFeature(Jingle.NAMESPACE);
		} catch (XMPPException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Add a Jingle session request listener to listen to incoming session
	 * requests.
	 * 
	 * @param li The listener
	 * 
	 * @see #removeJingleSessionRequestListener(JingleListener.SessionRequest)
	 * @see JingleListener
	 */
	public synchronized void addJingleSessionRequestListener(
			final JingleListener.SessionRequest li) {
		if (li != null) {
			if (listeners == null) {
				initJingleSessionRequestListeners();
			}
			synchronized (listeners) {
				listeners.add(li);
			}
		}
	}

	/**
	 * Removes a Jingle session listener.
	 * 
	 * @param li The jingle session listener to be removed
	 * @see #addJingleSessionRequestListener(JingleListener.SessionRequest)
	 * @see JingleListener
	 */
	public void removeJingleSessionRequestListener(final JingleListener.SessionRequest li) {
		if (listeners == null) {
			return;
		}
		synchronized (listeners) {
			listeners.remove(li);
		}
	}

	/**
	 * Register the listeners, waiting for a Jingle packet that tries to
	 * establish a new session.
	 */
	private void initJingleSessionRequestListeners() {
		PacketFilter initRequestFilter = new PacketFilter() {
			// Return true if we accept this packet
			public boolean accept(Packet pin) {
				if (pin instanceof IQ) {
					IQ iq = (IQ) pin;
					if (iq.getType().equals(IQ.Type.SET)) {
						if (iq instanceof Jingle) {
							Jingle jin = (Jingle) pin;
							if (jin.getAction().equals(Jingle.Action.SESSIONINITIATE)) {
								return true;
							}
						}
					}
				}
				return false;
			}
		};

		listeners = new ArrayList();

		// Start a packet listener for session initiation requests
		connection.addPacketListener(new PacketListener() {
			public void processPacket(final Packet packet) {
				triggerSessionRequested((Jingle) packet);
			}
		}, initRequestFilter);
	}

	/**
	 * Activates the listeners on a Jingle session request.
	 * 
	 * @param initJin The packet that must be passed to the listeners.
	 */
	protected void triggerSessionRequested(final Jingle initJin) {
		JingleListener.SessionRequest[] listeners = null;

		// Make a synchronized copy of the listeners
		synchronized (this.listeners) {
			listeners = new JingleListener.SessionRequest[this.listeners.size()];
			this.listeners.toArray(listeners);
		}

		// ... and let them know of the event
		JingleSessionRequest request = new JingleSessionRequest(this, initJin);
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].sessionRequested(request);
		}
	}

	// Session creation

	/**
	 * Creates an Jingle session to start a communication with another user.
	 * 
	 * @param responder The fully qualified jabber ID with resource of the other
	 *            user.
	 * @return The session on which the negotiation can be run.
	 */
	public OutgoingJingleSession createOutgoingJingleSession(final String responder,
			final List payloadTypes) {

		if (responder == null || StringUtils.parseName(responder).length() <= 0
				|| StringUtils.parseServer(responder).length() <= 0
				|| StringUtils.parseResource(responder).length() <= 0) {
			throw new IllegalArgumentException(
					"The provided user id was not fully qualified");
		}

		OutgoingJingleSession session = new OutgoingJingleSession(connection, responder,
				payloadTypes, resolver);
		return session;
	}

	/**
	 * When the session request is acceptable, this method should be invoked. It
	 * will create an JingleSession which allows the negotiation to procede.
	 * 
	 * @param request The remote request that is being accepted.
	 * @return The session which manages the rest of the negotiation.
	 */
	public IncomingJingleSession createIncomingJingleSession(
			final JingleSessionRequest request, final List payloadTypes) {
		if (request == null) {
			throw new NullPointerException("Received request cannot be null");
		}

		IncomingJingleSession session = new IncomingJingleSession(connection, request
				.getFrom(), payloadTypes, resolver);

		return session;
	}

	/**
	 * Reject the session. If we don't want to accept the new session, send an
	 * appropriate error packet.
	 * 
	 * @param request the request.
	 */
	protected void rejectIncomingJingleSession(final JingleSessionRequest request) {
		Jingle initiation = request.getJingle();

		IQ rejection = JingleSession.createError(initiation.getPacketID(), initiation
				.getFrom(), initiation.getTo(), 403, "Declined");
		connection.sendPacket(rejection);
	}
}
