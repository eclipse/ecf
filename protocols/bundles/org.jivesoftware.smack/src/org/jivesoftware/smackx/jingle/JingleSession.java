/**
 * $RCSfile: JingleSession.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/10/17 19:12:42 $
 *
 * Copyright (C) 2002-2006 Jive Software. All rights reserved.
 * ====================================================================
 * The Jive Software License (based on Apache Software License, Version 1.1)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by
 *        Jive Software (http://www.jivesoftware.com)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Smack" and "Jive Software" must not be used to
 *    endorse or promote products derived from this software without
 *    prior written permission. For written permission, please
 *    contact webmaster@jivesoftware.com.
 *
 * 5. Products derived from this software may not be called "Smack",
 *    nor may "Smack" appear in their name, without prior written
 *    permission of Jive Software.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL JIVE SOFTWARE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 */

package org.jivesoftware.smackx.jingle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smackx.nat.TransportCandidate;
import org.jivesoftware.smackx.packet.Jingle;
import org.jivesoftware.smackx.packet.JingleContentDescription;
import org.jivesoftware.smackx.packet.JingleContentInfo;
import org.jivesoftware.smackx.packet.JingleError;
import org.jivesoftware.smackx.packet.JingleTransport;
import org.jivesoftware.smackx.packet.JingleContentDescription.JinglePayloadType;
import org.jivesoftware.smackx.packet.JingleTransport.JingleTransportCandidate;

/**
 * A Jingle session.
 * 
 * </p>
 * 
 * This class contains some basic properties of every Jingle session. However,
 * the concrete implementation will be found in subclasses.
 * 
 * </p>
 * 
 * @see IncomingJingleSession
 * @see OutgoingJingleSession
 * 
 * </p>
 * @author Alvaro Saurin
 */
public abstract class JingleSession extends JingleNegotiator {

	// static

	private static final HashMap sessions = new HashMap();

	private static final Random randomGenerator = new Random();

	// non-static

	private String initiator; // Who started the communication

	private String responder; // The other endpoint

	private String sid; // A unique id that identifies this session

	private MediaNegotiator mediaNeg; // The description...

	private TransportNegotiator transNeg; // and transport negotiators

	PacketListener packetListener;

	PacketFilter packetFilter;

	/**
	 * Default constructor.
	 */
	public JingleSession(final XMPPConnection conn, final String ini, final String res,
			final String sessionid) {
		super(conn);

		mediaNeg = null;
		transNeg = null;

		initiator = ini;
		responder = res;
		sid = sessionid;

		// Add the session to the list and register the listeneres
		registerInstance();
		installConnectionListeners(conn);
	}

	/**
	 * Default constructor without session id.
	 */
	public JingleSession(final XMPPConnection conn, final String ini, final String res) {
		this(conn, ini, res, null);
	}

	/**
	 * Get the session initiator
	 * 
	 * @return the initiator
	 */
	public String getInitiator() {
		return initiator;
	}

	/**
	 * Set the session initiator
	 * 
	 * @param initiator the initiator to set
	 */
	public void setInitiator(final String initiator) {
		this.initiator = initiator;
	}

	/**
	 * Get the session responder
	 * 
	 * @return the responder
	 */
	public String getResponder() {
		return responder;
	}

	/**
	 * Set the session responder.
	 * 
	 * @param responder the receptor to set
	 */
	public void setResponder(final String responder) {
		this.responder = responder;
	}

	/**
	 * Get the session ID
	 * 
	 * @return the sid
	 */
	public String getSid() {
		return sid;
	}

	/**
	 * Set the session ID
	 * 
	 * @param sid the sid to set
	 */
	protected void setSid(final String sessionId) {
		sid = sessionId;
	}

	/**
	 * Generate a unique session ID.
	 */
	protected String generateSessionId() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(Math.abs(randomGenerator.nextLong()));

		return buffer.toString();
	}

	/**
	 * Obtain the description negotiator for this session
	 * 
	 * @return the description negotiator
	 */
	protected MediaNegotiator getMediaNeg() {
		return mediaNeg;
	}

	/**
	 * Set the media negotiator.
	 * 
	 * @param mediaNeg the description negotiator to set
	 */
	protected void setMediaNeg(final MediaNegotiator mediaNeg) {
		destroyMediaNeg();
		this.mediaNeg = mediaNeg;
	}

	/**
	 * Destroy the media negotiator.
	 */
	protected void destroyMediaNeg() {
		if (mediaNeg != null) {
			mediaNeg.close();
			mediaNeg = null;
		}
	}

	/**
	 * Obtain the transport negotiator for this session.
	 * 
	 * @return the transport negotiator instance
	 */
	protected TransportNegotiator getTransportNeg() {
		return transNeg;
	}

	/**
	 * @param transNeg the transNeg to set
	 */
	protected void setTransportNeg(final TransportNegotiator transNeg) {
		destroyTransportNeg();
		this.transNeg = transNeg;
	}

	/**
	 * Destroy the transport negotiator.
	 */
	protected void destroyTransportNeg() {
		if (transNeg != null) {
			transNeg.close();
			transNeg = null;
		}
	}

	/**
	 * Return true if the transport and content negotiators have finished
	 */
	public boolean isFullyEstablished() {
		if (!isValid()) {
			return false;
		}
		if (!getTransportNeg().isFullyEstablished()
				|| !getMediaNeg().isFullyEstablished()) {
			return false;
		}
		return true;
	}

	/**
	 * Return true if the session is valid (<i>ie</i>, it has all the required
	 * elements initialized).
	 * 
	 * @return true if the session is valid.
	 */
	public boolean isValid() {
		return mediaNeg != null && transNeg != null && sid != null && initiator != null;
	}

	/**
	 * Dispatch an incoming packet. The medthod is responsible for recognizing
	 * the packet type and, depending on the current state, deliverying the
	 * packet to the right event handler and wait for a response.
	 * 
	 * @param iq the packet received
	 * @return the new Jingle packet to send.
	 * @throws XMPPException
	 */
	public IQ dispatchIncomingPacket(final IQ iq, final String id) throws XMPPException {
		IQ jout = null;

		if (invalidState()) {
			throw new IllegalStateException(
					"Illegal state in dispatch packet in Session manager.");
		} else {
			if (iq == null) {
				// If there is no input packet, then we must be inviting...
				jout = getState().eventInvite();
			} else {
				if (iq.getType().equals(IQ.Type.ERROR)) {
					// Process errors
					getState().eventError(iq);
				} else if (iq.getType().equals(IQ.Type.RESULT)) {
					// Process ACKs
					if (isExpectedId(iq.getPacketID())) {
						jout = getState().eventAck(iq);
						removeExpectedId(iq.getPacketID());
					}
				} else if (iq instanceof Jingle) {
					// It is not an error: it is a Jingle packet...
					Jingle jin = (Jingle) iq;
					Jingle.Action action = jin.getAction();

					if (action != null) {
						if (action.equals(Jingle.Action.SESSIONACCEPT)) {
							jout = getState().eventAccept(jin);
						} else if (action.equals(Jingle.Action.SESSIONINFO)) {
							jout = getState().eventInfo(jin);
						} else if (action.equals(Jingle.Action.SESSIONINITIATE)) {
							jout = getState().eventInitiate(jin);
						} else if (action.equals(Jingle.Action.SESSIONREDIRECT)) {
							jout = getState().eventRedirect(jin);
						} else if (action.equals(Jingle.Action.SESSIONTERMINATE)) {
							jout = getState().eventTerminate(jin);
						}
					} else {
						jout = errorMalformedStanza(iq);
					}
				}
			}

			if (jout != null) {
				// Save the packet id, for recognizing ACKs...
				addExpectedId(jout.getPacketID());
			}
		}

		return jout;
	}

	/**
	 * Process and respond to an incomming packet.
	 * 
	 * This method is called from the packet listener dispatcher when a new
	 * packet has arrived. The medthod is responsible for recognizing the packet
	 * type and, depending on the current state, deliverying it to the right
	 * event handler and wait for a response. The response will be another
	 * Jingle packet that will be sent to the other endpoint.
	 * 
	 * @param iq the packet received
	 * @return the new Jingle packet to send.
	 * @throws XMPPException
	 */
	public synchronized IQ respond(final IQ iq) throws XMPPException {
		IQ response = null;

		if (isValid()) {
			String responseId = null;
			IQ sessionResponse = null;
			IQ descriptionResponse = null;
			IQ transportResponse = null;

			// Send the packet to the right event handler for the session...
			try {
				sessionResponse = dispatchIncomingPacket(iq, null);
				if (sessionResponse != null) {
					responseId = sessionResponse.getPacketID();
				}

				// ... and do the same for the Description and Transport
				// parts...
				if (mediaNeg != null) {
					descriptionResponse = mediaNeg.dispatchIncomingPacket(iq, responseId);
				}

				if (transNeg != null) {
					transportResponse = transNeg.dispatchIncomingPacket(iq, responseId);
				}

				// Acknowledge the IQ reception
				sendAck(iq);

				// ... and send all these parts in a Jingle response.
				response = sendJingleParts(iq, (Jingle) sessionResponse,
						(Jingle) descriptionResponse, (Jingle) transportResponse);

			} catch (JingleException e) {
				// Send an error message, if present
				JingleError error = e.getError();
				if (error != null) {
					sendFormattedError(iq, error);
				}

				// Notify the session end and close everything...
				triggerSessionClosedOnError(e);
				close();
			}
		}

		return response;
	}

	// Packet formatting and delivery

	/**
	 * Put together all the parts ina Jingle packet.
	 * 
	 * @return the new Jingle packet
	 */
	private Jingle sendJingleParts(final IQ iq, final Jingle jSes, final Jingle jDesc,
			final Jingle jTrans) {
		Jingle response = null;

		if (jSes != null) {
			jSes.addDescriptions(jDesc.getDescriptionsList());
			jSes.addTransports(jTrans.getTransportsList());

			response = sendFormattedJingle(iq, jSes);
		} else {
			// If we don't have a valid session message, then we must send
			// separated messages for transport and media...
			if (jDesc != null) {
				response = sendFormattedJingle(iq, jDesc);
			}

			if (jTrans != null) {
				response = sendFormattedJingle(iq, jTrans);
			}
		}

		return response;
	}

	/**
	 * Complete and send an error. Complete all the null fields in an IQ error
	 * reponse, using the sesssion information we have or some info from the
	 * incoming packet.
	 * 
	 * @param jin The Jingle packet we are responing to
	 * @param pout the IQ packet we want to complete and send
	 */
	protected IQ sendFormattedError(final IQ iq, final JingleError error) {
		IQ perror = null;
		if (error != null) {
			perror = createIQ(getSid(), iq.getFrom(), iq.getTo(), IQ.Type.ERROR);

			// Fill in the fields with the info from the Jingle packet
			perror.setPacketID(iq.getPacketID());
			perror.addExtension(error);

			getConnection().sendPacket(perror);
		}
		return perror;
	}

	/**
	 * Complete and send a packet. Complete all the null fields in a Jingle
	 * reponse, using the session information we have or some info from the
	 * incoming packet.
	 * 
	 * @param jin The Jingle packet we are responing to
	 * @param jout the Jingle packet we want to complete and send
	 */
	protected Jingle sendFormattedJingle(final IQ iq, final Jingle jout) {
		if (jout != null) {
			if (jout.getInitiator() == null) {
				jout.setInitiator(getInitiator());
			}

			if (jout.getResponder() == null) {
				jout.setResponder(getResponder());
			}

			if (jout.getSid() == null) {
				jout.setSid(getSid());
			}

			String me = getConnection().getUser();
			String other = getResponder().equals(me) ? getInitiator() : getResponder();

			if (jout.getTo() == null) {
				if (iq != null) {
					jout.setTo(iq.getFrom());
				} else {
					jout.setTo(other);
				}
			}

			if (jout.getFrom() == null) {
				if (iq != null) {
					jout.setFrom(iq.getTo());
				} else {
					jout.setFrom(me);
				}
			}

			getConnection().sendPacket(jout);
		}
		return jout;
	}

	/**
	 * Complete and send a packet. Complete all the null fields in a Jingle
	 * reponse, using the session information we have.
	 * 
	 * @param jout the Jingle packet we want to complete and send
	 */
	protected Jingle sendFormattedJingle(final Jingle jout) {
		return sendFormattedJingle(null, jout);
	}

	/**
	 * Send an error indicating that the stanza is malformed.
	 * 
	 * @param iq
	 */
	protected IQ errorMalformedStanza(final IQ iq) {
		// FIXME: implement with the right message...
		return createError(iq.getPacketID(), iq.getFrom(), getConnection().getUser(),
				400, "Bad Request");
	}

	/**
	 * Check if we have an established session and, in that case, send an Accept
	 * packet.
	 */
	protected Jingle sendAcceptIfFullyEstablished() {
		Jingle result = null;
		if (isFullyEstablished()) {
			// Ok, send a packet saying that we accept this session
			Jingle jout = new Jingle(Jingle.Action.SESSIONACCEPT);
			jout.setType(IQ.Type.SET);

			result = sendFormattedJingle(jout);
		}
		return result;
	}

	/**
	 * Acknowledge a IQ packet.
	 * 
	 * @param iq The IQ to acknowledge
	 */
	private IQ sendAck(final IQ iq) {
		IQ result = null;

		if (iq != null) {
			// Don't acknowledge ACKs, errors...
			if (iq.getType().equals(IQ.Type.SET)) {
				IQ ack = createIQ(iq.getPacketID(), iq.getFrom(), iq.getTo(),
						IQ.Type.RESULT);

				getConnection().sendPacket(ack);
				result = ack;
			}
		}
		return result;
	}

	/**
	 * Send a content info message.
	 */
	public synchronized void sendContentInfo(final ContentInfo ci) {
		if (isValid()) {
			sendFormattedJingle(new Jingle(new JingleContentInfo(ci)));
		}
	}

	/**
	 * Get the content description the other part has accepted.
	 * 
	 * @param jin The Jingle packet where they have accepted the session.
	 * @return The audio PayloadType they have accepted.
	 * @throws XMPPException
	 */
	protected PayloadType.Audio getAcceptedAudioPayloadType(final Jingle jin)
			throws XMPPException {
		PayloadType.Audio acceptedPayloadType = null;
		ArrayList jda = jin.getDescriptionsList();

		if (jin.getAction().equals(Jingle.Action.SESSIONACCEPT)) {

			if (jda.size() > 1) {
				throw new XMPPException(
						"Unsupported feature: the number of accepted content descriptions is greater than 1.");
			} else if (jda.size() == 1) {
				JingleContentDescription jd = (JingleContentDescription) jda.get(0);
				if (jd.getJinglePayloadTypesCount() > 1) {
					throw new XMPPException(
							"Unsupported feature: the number of accepted payload types is greater than 1.");
				}
				if (jd.getJinglePayloadTypesCount() == 1) {
					JinglePayloadType jpt = (JinglePayloadType) jd
							.getJinglePayloadTypesList().get(0);
					acceptedPayloadType = (PayloadType.Audio) jpt.getPayloadType();
				}
			}
		}
		return acceptedPayloadType;
	}

	/**
	 * Get the accepted local candidate we have previously offered.
	 * 
	 * @param jin The jingle packet where they accept the session
	 * @return The transport candidate they have accepted.
	 * @throws XMPPException
	 */
	protected TransportCandidate getAcceptedLocalCandidate(final Jingle jin)
			throws XMPPException {
		ArrayList jta = jin.getTransportsList();
		TransportCandidate acceptedLocalCandidate = null;

		if (jin.getAction().equals(Jingle.Action.SESSIONACCEPT)) {
			if (jta.size() > 1) {
				throw new XMPPException(
						"Unsupported feature: the number of accepted transports is greater than 1.");
			} else if (jta.size() == 1) {
				JingleTransport jt = (JingleTransport) jta.get(0);

				if (jt.getCandidatesCount() > 1) {
					throw new XMPPException(
							"Unsupported feature: the number of accepted transport candidates is greater than 1.");
				} else if (jt.getCandidatesCount() == 1) {
					JingleTransportCandidate jtc = (JingleTransportCandidate) jt
							.getCandidatesList().get(0);
					acceptedLocalCandidate = jtc.getMediaTransport();
				}
			}
		}

		return acceptedLocalCandidate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return Jingle.getSessionHash(getSid(), getInitiator());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final JingleSession other = (JingleSession) obj;

		if (initiator == null) {
			if (other.initiator != null) {
				return false;
			}
		} else if (!initiator.equals(other.initiator)) {
			return false;
		}

		if (responder == null) {
			if (other.responder != null) {
				return false;
			}
		} else if (!responder.equals(other.responder)) {
			return false;
		}

		if (sid == null) {
			if (other.sid != null) {
				return false;
			}
		} else if (!sid.equals(other.sid)) {
			return false;
		}

		return true;
	}

	// Instances management

	/**
	 * Clean a session from the list.
	 * 
	 * @param connection The connection to clean up
	 */
	private void unregisterInstanceFor(final XMPPConnection connection) {
		synchronized (sessions) {
			sessions.remove(connection);
		}
	}

	/**
	 * Register this instance.
	 */
	private void registerInstance() {
		synchronized (sessions) {
			sessions.put(getConnection(), this);
		}
	}

	/**
	 * Returns the JingleSession related to a particular connection.
	 * 
	 * @param con A XMPP connection
	 * @return a Jingle session
	 */
	public static JingleSession getInstanceFor(final XMPPConnection con) {
		if (con == null) {
			throw new IllegalArgumentException("Connection cannot be null");
		}

		JingleSession result = null;
		synchronized (sessions) {
			if (sessions.containsKey(con)) {
				result = (JingleSession) sessions.get(con);
			}
		}

		return result;
	}

	/**
	 * Configure a session, setting some action listeners...
	 * 
	 * @param session The connection to set up
	 */
	private void installConnectionListeners(final XMPPConnection connection) {
		if (connection != null) {
			connection.addConnectionListener(new ConnectionListener() {
				public void connectionClosed() {
					unregisterInstanceFor(connection);
				}

				public void connectionClosedOnError(final java.lang.Exception e) {
					unregisterInstanceFor(connection);
				}
			});
		}
	}

	/**
	 * Remove the packet listener used for processing packet.
	 */
	protected void removePacketListener() {
		if (packetListener != null) {
			getConnection().removePacketListener(packetListener);
		}
	}

	/**
	 * Install the packet listener. The listener is responsible for responding
	 * to any packet that we receive...
	 */
	protected void updatePacketListener() {
		removePacketListener();

		packetListener = new PacketListener() {
			public void processPacket(final Packet packet) {
				try {
					respond((IQ) packet);
				} catch (XMPPException e) {
					e.printStackTrace();
				}
			}
		};

		packetFilter = new PacketFilter() {
			public boolean accept(final Packet packet) {
				if (packet instanceof IQ) {
					IQ iq = (IQ) packet;

					String me = getConnection().getUser();

					if (!iq.getTo().equals(me)) {
						return false;
					}

					String other = getResponder().equals(me) ? getInitiator()
							: getResponder();

					if (!iq.getFrom().equals(other)) {
						return false;
					}

					if (iq instanceof Jingle) {
						Jingle jin = (Jingle) iq;

						String sid = jin.getSid();
						if (!sid.equals(getSid())) {
							return false;
						}
						String ini = jin.getInitiator();
						if (!ini.equals(getInitiator())) {
							return false;
						}
					} else {
						// We accept some non-Jingle IQ packets: ERRORs and ACKs
						if (iq.getType().equals(IQ.Type.SET)) {
							return false;
						} else if (iq.getType().equals(IQ.Type.GET)) {
							return false;
						}
					}
					return true;
				}
				return false;
			}
		};

		getConnection().addPacketListener(packetListener, packetFilter);
	}

	// Listeners

	/**
	 * Add a listener for media negotiation events
	 * 
	 * @param li The listener
	 */
	public void addMediaListener(final JingleListener.Media li) {
		if (getMediaNeg() != null) {
			getMediaNeg().addListener(li);
		}
	}

	/**
	 * Remove a listener for media negotiation events
	 * 
	 * @param li The listener
	 */
	public void removeMediaListener(final JingleListener.Media li) {
		if (getMediaNeg() != null) {
			getMediaNeg().removeListener(li);
		}
	}

	/**
	 * Add a listener for transport negotiation events
	 * 
	 * @param li The listener
	 */
	public void addTransportListener(final JingleListener.Transport li) {
		if (getTransportNeg() != null) {
			getTransportNeg().addListener(li);
		}
	}

	/**
	 * Remove a listener for transport negotiation events
	 * 
	 * @param li The listener
	 */
	public void removeTransportListener(final JingleListener.Transport li) {
		if (getTransportNeg() != null) {
			getTransportNeg().removeListener(li);
		}
	}

	// Triggers

	/**
	 * Trigger a session closed event.
	 */
	protected void triggerSessionClosed(final String reason) {
		ArrayList listeners = getListenersList();
		Iterator iter = listeners.iterator();
		while (iter.hasNext()) {
			JingleListener li = (JingleListener) iter.next();
			if (li instanceof JingleListener.Session) {
				JingleListener.Session sli = (JingleListener.Session) li;
				sli.sessionClosed(reason);
			}
		}
	}

	/**
	 * Trigger a session closed event due to an error.
	 */
	protected void triggerSessionClosedOnError(final XMPPException exc) {
		ArrayList listeners = getListenersList();
		Iterator iter = listeners.iterator();
		while (iter.hasNext()) {
			JingleListener li = (JingleListener) iter.next();
			if (li instanceof JingleListener.Session) {
				JingleListener.Session sli = (JingleListener.Session) li;
				sli.sessionClosedOnError(exc);
			}
		}
	}

	/**
	 * Trigger a session established event.
	 */
	protected void triggerSessionEstablished(final PayloadType pt,
			final TransportCandidate rc, final TransportCandidate lc) {
		ArrayList listeners = getListenersList();
		Iterator iter = listeners.iterator();
		while (iter.hasNext()) {
			JingleListener li = (JingleListener) iter.next();
			if (li instanceof JingleListener.Session) {
				JingleListener.Session sli = (JingleListener.Session) li;
				sli.sessionEstablished(pt, rc, lc);
			}
		}
	}

	/**
	 * Trigger a session redirect event.
	 */
	protected void triggerSessionRedirect(final String arg) {
		ArrayList listeners = getListenersList();
		Iterator iter = listeners.iterator();
		while (iter.hasNext()) {
			JingleListener li = (JingleListener) iter.next();
			if (li instanceof JingleListener.Session) {
				JingleListener.Session sli = (JingleListener.Session) li;
				sli.sessionRedirected(arg);
			}
		}
	}

	/**
	 * Trigger a session redirect event.
	 */
	protected void triggerSessionDeclined(final String reason) {
		ArrayList listeners = getListenersList();
		Iterator iter = listeners.iterator();
		while (iter.hasNext()) {
			JingleListener li = (JingleListener) iter.next();
			if (li instanceof JingleListener.Session) {
				JingleListener.Session sli = (JingleListener.Session) li;
				sli.sessionDeclined(reason);
			}
		}
	}

	/**
	 * Start the negotiation.
	 * 
	 * @throws JingleException
	 * @throws XMPPException
	 */
	public abstract void start(final JingleSessionRequest jin) throws XMPPException;

	/**
	 * Terminate negotiations.
	 */
	public void close() {
		destroyMediaNeg();
		destroyTransportNeg();

		removePacketListener();

		super.close();
	}

	// Packet and error creation

	/**
	 * A convience method to create an IQ packet.
	 * 
	 * @param ID The packet ID of the
	 * @param to To whom the packet is addressed.
	 * @param from From whom the packet is sent.
	 * @param type The iq type of the packet.
	 * @return The created IQ packet.
	 */
	public static IQ createIQ(final String ID, final String to, final String from,
			final IQ.Type type) {
		IQ iqPacket = new IQ() {
			public String getChildElementXML() {
				return null;
			}
		};

		iqPacket.setPacketID(ID);
		iqPacket.setTo(to);
		iqPacket.setFrom(from);
		iqPacket.setType(type);

		return iqPacket;
	}

	/**
	 * A convience method to create an error packet.
	 * 
	 * @param ID The packet ID of the
	 * @param to To whom the packet is addressed.
	 * @param from From whom the packet is sent.
	 * @param errCode The error code.
	 * @param errStr The error string.
	 * 
	 * @return The created IQ packet.
	 */
	public static IQ createError(final String ID, final String to, final String from,
			final int errCode, final String errStr) {

		IQ iqError = createIQ(ID, to, from, IQ.Type.ERROR);
		XMPPError error = new XMPPError(errCode, errStr);
		iqError.setError(error);

		return iqError;
	}
}
