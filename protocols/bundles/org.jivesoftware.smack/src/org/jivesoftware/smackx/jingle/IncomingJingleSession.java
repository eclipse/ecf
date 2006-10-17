/**
 * $RCSfile: IncomingJingleSession.java,v $
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

import java.util.List;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smackx.nat.TransportCandidate;
import org.jivesoftware.smackx.nat.TransportResolver;
import org.jivesoftware.smackx.packet.Jingle;
import org.jivesoftware.smackx.packet.JingleContentDescription;
import org.jivesoftware.smackx.packet.JingleError;
import org.jivesoftware.smackx.packet.JingleContentDescription.JinglePayloadType;

/**
 * An incoming Jingle session.
 * 
 * </p>
 * 
 * This class is not directly used by users. Instead, users should refer to the
 * JingleManager class, that will create the appropiate instance...
 * 
 * </p>
 * 
 * @author Alvaro Saurin
 */
public class IncomingJingleSession extends JingleSession {

	// states
	private final Accepting accepting;

	private final Pending pending;

	private final Active active;

	/**
	 * Constructor with the request
	 * 
	 * @param conn the XMPP connection
	 * @param responder the responder
	 * @param resolver The transport resolver
	 */
	public IncomingJingleSession(final XMPPConnection conn, final String responder,
			final List payloadTypes, final TransportResolver resolver) {

		super(conn, responder, conn.getUser());

		// Create the states...

		accepting = new Accepting(this);
		pending = new Pending(this);
		active = new Active(this);

		setMediaNeg(new MediaNegotiator(this, payloadTypes));
		setTransportNeg(new TransportNegotiator.RawUdp(this, resolver));
	}

	/**
	 * Start the session.
	 * 
	 * @throws XMPPException
	 */
	public void start(final JingleSessionRequest request) throws XMPPException {
		if (invalidState()) {
			Jingle jin = request.getJingle();
			if (jin != null) {

				// Initialize the session information
				setSid(jin.getSid());

				// Establish the default state
				setState(accepting);

				updatePacketListener();
				respond(jin);
			} else {
				throw new IllegalStateException(
						"Session request with null Jingle packet.");
			}
		} else {
			throw new IllegalStateException("Starting session without null state.");
		}
	}

	// States

	/**
	 * First stage when we have received a session request, and we accept the
	 * request. We start in this stage, as the instance is created when the user
	 * accepts the connection...
	 */
	public class Accepting extends JingleNegotiator.State {

		public Accepting(final JingleNegotiator neg) {
			super(neg);
		}

		/**
		 * Initiate the incoming session. We have already sent the ACK partially
		 * accepting the session...
		 * 
		 * @throws XMPPException
		 */
		public Jingle eventInitiate(final Jingle inJingle) throws XMPPException {
			// Set the new session state
			setState(pending);
			return super.eventInitiate(inJingle);
		}

		/**
		 * An error has occurred.
		 * 
		 * @throws XMPPException
		 */
		public void eventError(final IQ iq) throws XMPPException {
			triggerSessionClosedOnError(new JingleException(iq.getError().getMessage()));
			super.eventError(iq);
		}
	}

	/**
	 * "Pending" state: we are waiting for the transport and content
	 * negotiators.
	 */
	private class Pending extends JingleNegotiator.State {

		JingleListener.Media mediaListener;

		JingleListener.Transport transportListener;

		public Pending(final JingleNegotiator neg) {
			super(neg);

			// Create the listeners that will send a "session-accept" when the
			// sub-negotiators are done.
			mediaListener = new JingleListener.Media() {
				public void mediaClosed(final PayloadType cand) {
				}

				public void mediaEstablished(final PayloadType pt) {
					checkFullyEstablished();
				}
			};

			transportListener = new JingleListener.Transport() {
				public void transportEstablished(final TransportCandidate local,
						final TransportCandidate remote) {
					checkFullyEstablished();
				}

				public void transportClosed(final TransportCandidate cand) {
				}

				public void transportClosedOnError(final XMPPException e) {
				}
			};
		}

		/**
		 * Enter in the pending state: wait for the sub-negotiators.
		 * 
		 * @see org.jivesoftware.smackx.jingle.JingleNegotiator.State#eventEnter()
		 */
		public void eventEnter() {
			// Add the listeners to the sub-negotiators...
			addMediaListener(mediaListener);
			addTransportListener(transportListener);
			super.eventEnter();
		}

		/**
		 * Exit of the state
		 * 
		 * @see org.jivesoftware.smackx.jingle.JingleNegotiator.State#eventExit()
		 */
		public void eventExit() {
			removeMediaListener(mediaListener);
			removeTransportListener(transportListener);
			super.eventExit();
		}

		/**
		 * Check if the session has been fully accepted by all the
		 * sub-negotiators and, in that case, send an "accept" message...
		 */
		private void checkFullyEstablished() {
			if (isFullyEstablished()) {

				PayloadType.Audio bestCommonAudioPt = getMediaNeg()
						.getBestCommonAudioPt();
				TransportCandidate bestRemoteCandidate = getTransportNeg()
						.getBestRemoteCandidate();
				TransportCandidate acceptedLocalCandidate = getTransportNeg()
						.getAcceptedLocalCandidate();

				if (bestCommonAudioPt != null && bestRemoteCandidate != null
						&& acceptedLocalCandidate != null) {
					// Ok, send a packet saying that we accept this session
					Jingle jout = new Jingle(Jingle.Action.SESSIONACCEPT);

					// ... with the audio payload type and the transport
					// candidate
					jout.addDescription(new JingleContentDescription.Audio(
							new JinglePayloadType(bestCommonAudioPt)));
					jout.addTransport(getTransportNeg().getJingleTransport(
							bestRemoteCandidate));

					addExpectedId(jout.getPacketID());
					sendFormattedJingle(jout);
				}
			}
		}

		/**
		 * The other endpoint has accepted the session.
		 */
		public Jingle eventAccept(final Jingle jin) throws XMPPException {

			PayloadType acceptedPayloadType = null;
			TransportCandidate acceptedLocalCandidate = null;

			// We process the "accepted" if we have finished the
			// sub-negotiators. Maybe this is not needed (ie, the other endpoint
			// can take the first valid transport candidate), but otherwise we
			// must cancel the negotiators...
			//
			if (isFullyEstablished()) {
				acceptedPayloadType = getAcceptedAudioPayloadType(jin);
				acceptedLocalCandidate = getAcceptedLocalCandidate(jin);

				if (acceptedPayloadType != null && acceptedLocalCandidate != null) {
					if (acceptedPayloadType.equals(getMediaNeg().getBestCommonAudioPt())
							&& acceptedLocalCandidate.equals(getTransportNeg()
									.getAcceptedLocalCandidate())) {
						setState(active);
					}
				} else {
					throw new JingleException(JingleError.MALFORMED_STANZA);
				}
			}

			return super.eventAccept(jin);
		}

		/**
		 * We have received a confirmation.
		 * 
		 * @see org.jivesoftware.smackx.jingle.JingleNegotiator.State#eventAck(org.jivesoftware.smack.packet.IQ)
		 */
		public Jingle eventAck(final IQ iq) throws XMPPException {
			setState(active);
			return super.eventAck(iq);
		}

		/**
		 * An error has occurred.
		 * 
		 * @throws XMPPException
		 */
		public void eventError(final IQ iq) throws XMPPException {
			triggerSessionClosedOnError(new XMPPException(iq.getError().getMessage()));
			super.eventError(iq);
		}
	}

	/**
	 * "Active" state: we have an agreement about the session.
	 */
	private class Active extends JingleNegotiator.State {
		public Active(final JingleNegotiator neg) {
			super(neg);
		}

		/**
		 * We have a established session: notify the listeners
		 * 
		 * @see org.jivesoftware.smackx.jingle.JingleNegotiator.State#eventEnter()
		 */
		public void eventEnter() {
			PayloadType.Audio bestCommonAudioPt = getMediaNeg().getBestCommonAudioPt();
			TransportCandidate bestRemoteCandidate = getTransportNeg()
					.getBestRemoteCandidate();
			TransportCandidate acceptedLocalCandidate = getTransportNeg()
					.getAcceptedLocalCandidate();

			// Trigger the session established flag
			triggerSessionEstablished(bestCommonAudioPt, bestRemoteCandidate,
					acceptedLocalCandidate);

			super.eventEnter();
		}

		/**
		 * Terminate the connection.
		 * 
		 * @see org.jivesoftware.smackx.jingle.JingleNegotiator.State#eventTerminate(org.jivesoftware.smackx.packet.Jingle)
		 */
		public Jingle eventTerminate(final Jingle jin) throws XMPPException {
			triggerSessionClosed(null);
			return super.eventTerminate(jin);
		}

		/**
		 * An error has occurred.
		 * 
		 * @throws XMPPException
		 */
		public void eventError(final IQ iq) throws XMPPException {
			triggerSessionClosedOnError(new XMPPException(iq.getError().getMessage()));
			super.eventError(iq);
		}
	}
}
