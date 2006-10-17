package org.jivesoftware.smackx.jingle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smackx.nat.TransportCandidate;
import org.jivesoftware.smackx.nat.TransportResolver;
import org.jivesoftware.smackx.nat.TransportResolverListener;
import org.jivesoftware.smackx.packet.Jingle;
import org.jivesoftware.smackx.packet.JingleTransport;
import org.jivesoftware.smackx.packet.JingleTransport.JingleTransportCandidate;

/**
 * Transport negotiator.
 * 
 * </p>
 * 
 * This class is responsible for managing the transport negotiation process,
 * handling all the packet interchange and the stage control.
 * 
 * </p>
 * 
 * @author Alvaro Saurin <alvaro.saurin@gmail.com>
 */
public abstract class TransportNegotiator extends JingleNegotiator {

	// The time we give to the candidates check before we accept or decline the
	// transport (in milliseconds)
	public final static int CANDIDATES_ACCEPT_PERIOD = 4000;

	// The session this nenotiator belongs to
	private JingleSession session;

	// The transport manager
	private final TransportResolver resolver;

	// Transport candidates we have offered
	private final List offeredCandidates = new ArrayList();

	// List of remote transport candidates
	private final List remoteCandidates = new ArrayList();

	// Valid remote candidates
	private final List validRemoteCandidates = new ArrayList();

	// The best local candidate we have offered (and accepted by the other part)
	private TransportCandidate acceptedLocalCandidate;

	// The thread that will report the result to the other end
	private Thread resultThread;

	// Listener for the resolver
	private TransportResolverListener.Resolver resolverListener;

	// states
	final Inviting inviting;

	final Accepting accepting;

	final Pending pending;

	final Active active;

	/**
	 * Default constructor.
	 * 
	 * @param js The Jingle session
	 * @param transResolver The TransportManager to use
	 */
	public TransportNegotiator(final JingleSession js,
			final TransportResolver transResolver) {
		super(js.getConnection());

		session = js;
		resolver = transResolver;

		resultThread = null;

		// Create the states...
		inviting = new Inviting(this);
		accepting = new Accepting(this);
		pending = new Pending(this);
		active = new Active(this);
	}

	/**
	 * Get a new instance of the right JingleTransport class with this
	 * candidate.
	 * 
	 * @return A JingleTransport instance
	 */
	protected abstract JingleTransport getJingleTransport(TransportCandidate cand);

	/**
	 * Return true if the transport candidate is acceptable for the current
	 * negotiator.
	 * 
	 * @return true if the transport candidate is acceptable
	 */
	protected abstract boolean acceptableTransportCandidate(TransportCandidate tc);

	/**
	 * Obtain the best local candidate we want to offer.
	 * 
	 * @return the best local candidate
	 */
	public TransportCandidate getBestLocalCandidate() {
		return resolver.getPreferredCandidate();
	}

	/**
	 * Set the best local transport candidate we have offered and accepted by
	 * the other endpoint.
	 * 
	 * @param acceptedLocalCandidate the acceptedLocalCandidate to set
	 */
	protected void setAcceptedLocalCandidate(final TransportCandidate bestLocalCandidate)
			throws XMPPException {
		if (resolver.getCandidatesList().contains(bestLocalCandidate)) {
			acceptedLocalCandidate = bestLocalCandidate;
		} else {
			throw new XMPPException("Local transport candidate has not be offered.");
		}
	}

	/**
	 * Get the best accepted local candidate we have offered.
	 * 
	 * @return a transport candidate we have offered.
	 */
	public TransportCandidate getAcceptedLocalCandidate() {
		return acceptedLocalCandidate;
	}

	/**
	 * Obtain the best common transport candidate obtained in the negotiation.
	 * 
	 * @return the bestRemoteCandidate
	 */
	public abstract TransportCandidate getBestRemoteCandidate();

	/**
	 * Get the list of remote candidates.
	 * 
	 * @return the remoteCandidates
	 */
	public List getRemoteCandidates() {
		return remoteCandidates;
	}

	/**
	 * Add a remote candidate to the list. The candidate will be checked in
	 * order to verify if it is usable.
	 * 
	 * @param rc a remote candidate to add and check.
	 */
	public void addRemoteCandidate(final TransportCandidate rc) {
		// Add the candidate to the list
		if (rc != null) {
			if (acceptableTransportCandidate(rc)) {
				synchronized (remoteCandidates) {
					remoteCandidates.add(rc);
				}

				// Check if the new candidate can be used.
				checkRemoteCandidate(rc);
			}
		}
	}

	/**
	 * Add a offered candidate to the list.
	 * 
	 * @param rc a remote candidate we have offered.
	 */
	public void addOfferedCandidate(final TransportCandidate rc) {
		// Add the candidate to the list
		if (rc != null) {
			synchronized (offeredCandidates) {
				offeredCandidates.add(rc);
			}
		}
	}

	/**
	 * Check asynchronously the new transport candidate.
	 * 
	 * @param offeredCandidate a transport candidates to check
	 */
	protected void checkRemoteCandidate(final TransportCandidate offeredCandidate) {
		resolver.addListener(new TransportResolverListener.Checker() {
			public void candidateChecked(final TransportCandidate cand,
					final boolean validCandidate) {
				if (validCandidate) {
					addValidRemoteCandidate(offeredCandidate);
				}
			}
		});
		resolver.check(offeredCandidate);
	}

	/**
	 * Return true if the transport is established.
	 * 
	 * @return true if the transport is established.
	 */
	public boolean isEstablished() {
		return getBestRemoteCandidate() != null && getAcceptedLocalCandidate() != null;
	}

	/**
	 * Return true if the transport is fully established.
	 * 
	 * @return true if the transport is fully established.
	 */
	public boolean isFullyEstablished() {
		return isEstablished() && getState() == active;
	}

	/**
	 * Launch a thread that checks, after some time, if any of the candidates
	 * offered by the other endpoint is usable. The thread does not check the
	 * candidates: it just checks if we have got a valid one and sends an Accept
	 * in that case.
	 */
	private void delayedCheckBestCandidate(final JingleSession js, final Jingle jin) {
		//
		// If this is the first insertion in the list, start the thread that
		// will send the result of our checks...
		//
		if (resultThread == null && !getRemoteCandidates().isEmpty()) {
			resultThread = new Thread(new Runnable() {
				public void run() {

					// Sleep for some time, waiting for the candidates checks
					try {
						Thread.sleep(CANDIDATES_ACCEPT_PERIOD
								+ TransportResolver.CHECK_TIMEOUT);
					} catch (InterruptedException e) {
					}

					// Once we are in pending state, look for any valid remote
					// candidate, and send an "accept" if we have one...
					TransportCandidate bestRemote = getBestRemoteCandidate();
					State state = getState();

					if (bestRemote != null && (state == pending || state == active)) {
						// Accepting the remote candidate
						Jingle jout = new Jingle(Jingle.Action.TRANSPORTACCEPT);
						jout.addTransport(getJingleTransport(bestRemote));

						// Send the packet
						js.sendFormattedJingle(jin, jout);

						if (isEstablished()) {
							setState(active);
						}
					}
				}
			}, "Waiting for all the transport candidates checks...");

			resultThread.setName("Transport Resolver Result");
			resultThread.start();
		}
	}

	/**
	 * Add a valid remote candidate to the list. The remote candidate has been
	 * checked, and the remote
	 * 
	 * @param remoteCandidate a remote candidate to add
	 */
	public void addValidRemoteCandidate(final TransportCandidate remoteCandidate) {
		// Add the candidate to the list
		if (remoteCandidate != null) {
			synchronized (validRemoteCandidates) {
				validRemoteCandidates.add(remoteCandidate);
			}
		}
	}

	/**
	 * Get the list of valid (ie, checked) remote candidates.
	 * 
	 * @return The list of valid (ie, already checked) remote candidates.
	 */
	public ArrayList getValidRemoteCandidatesList() {
		synchronized (validRemoteCandidates) {
			return new ArrayList(validRemoteCandidates);
		}
	}

	/**
	 * Get an iterator for the list of valid (ie, checked) remote candidates.
	 * 
	 * @return The iterator for the list of valid (ie, already checked) remote
	 *         candidates.
	 */
	public Iterator getValidRemoteCandidates() {
		return Collections.unmodifiableList(getRemoteCandidates()).iterator();
	}

	/**
	 * Add an offered remote candidate. The transport candidate can be unusable:
	 * we must check if we can use it.
	 * 
	 * @param rc the remote candidate to add.
	 */
	public void addRemoteCandidates(final List rc) {
		if (rc != null) {
			if (rc.size() > 0) {
				Iterator iter = rc.iterator();
				while (iter.hasNext()) {
					addRemoteCandidate((TransportCandidate) iter.next());
				}
			}
		}
	}

	/**
	 * Parse the list of transport candidates from a Jingle packet.
	 * 
	 * @param jin The input jingle packet
	 */
	private ArrayList obtainCandidatesList(final Jingle jin) {
		ArrayList result = new ArrayList();

		if (jin != null) {
			// Get the list of candidates from the packet
			Iterator iTrans = jin.getTransports();
			while (iTrans.hasNext()) {
				JingleTransport trans = (JingleTransport) iTrans.next();

				Iterator iCand = trans.getCandidates();
				while (iCand.hasNext()) {
					JingleTransportCandidate cand = (JingleTransportCandidate) iCand
							.next();
					TransportCandidate transCand = cand.getMediaTransport();
					result.add(transCand);
				}
			}
		}

		return result;
	}

	final private boolean isOfferStarted() {
		return resolver.isResolving() || resolver.isResolved();
	}

	/**
	 * Send an offer for a transport candidate
	 * 
	 * @param cand
	 */
	private synchronized void sendTransportCandidateOffer(final TransportCandidate cand) {
		if (!cand.isNull()) {
			addOfferedCandidate(cand);

			// Offer our new candidate...
			session.sendFormattedJingle(new Jingle(getJingleTransport(cand)));
		}
	}

	/**
	 * Create a Jingle packet where we announce our transport candidates.
	 * 
	 * @throws XMPPException
	 */
	protected void sendTransportCandidatesOffer() throws XMPPException {
		List notOffered = resolver.getCandidatesList();
		notOffered.removeAll(offeredCandidates);

		// Send any unset candidate
		Iterator iter = notOffered.iterator();
		while (iter.hasNext()) {
			sendTransportCandidateOffer((TransportCandidate) iter.next());
		}

		// .. and start a listener that will send any future candidate
		if (resolverListener == null) {
			// Add a listener that sends the offer when the resolver finishes...
			resolverListener = new TransportResolverListener.Resolver() {
				public void candidateAdded(final TransportCandidate cand) {
					sendTransportCandidateOffer(cand);
				}

				public void end() {
				}

				public void init() {
				}
			};

			resolver.addListener(resolverListener);
		}

		if (!(resolver.isResolving() || resolver.isResolved())) {
			// Resolve our IP and port
			resolver.resolve();
		}
	}

	/**
	 * Dispatch an incoming packet. The method is responsible for recognizing
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
			if (iq == null) {
				// With a null packet, we are just inviting the other end...
				setState(inviting);
				jout = getState().eventInvite();

			} else {
				if (iq instanceof Jingle) {
					// If there is no specific media action associated, then we
					// are being invited to a new session...
					setState(accepting);
					jout = getState().eventInitiate((Jingle) iq);
				} else {
					throw new IllegalStateException(
							"Invitation IQ received is not a Jingle packet in Transport negotiator.");
				}
			}
		} else {
			if (iq == null) {
				return null;
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
					// Get the action from the Jingle packet
					Jingle jin = (Jingle) iq;
					Jingle.Action action = jin.getAction();

					if (action != null) {
						if (action.equals(Jingle.Action.TRANSPORTACCEPT)) {
							jout = getState().eventAccept(jin);
						} else if (action.equals(Jingle.Action.TRANSPORTDECLINE)) {
							jout = getState().eventDecline(jin);
						} else if (action.equals(Jingle.Action.TRANSPORTINFO)) {
							jout = getState().eventInfo(jin);
						} else if (action.equals(Jingle.Action.TRANSPORTMODIFY)) {
							jout = getState().eventModify(jin);
						}
					}
				}
			}
		}

		// Save the Id for any ACK
		if (id != null) {
			addExpectedId(id);
		} else {
			if (jout != null) {
				addExpectedId(jout.getPacketID());
			}
		}

		return jout;
	}

	/**
	 * Trigger a session established event.
	 * 
	 * @param best payload type that has been agreed.
	 */
	protected void triggerTransportEstablished(final TransportCandidate local,
			final TransportCandidate remote) {
		ArrayList listeners = getListenersList();
		Iterator iter = listeners.iterator();
		while (iter.hasNext()) {
			JingleListener li = (JingleListener) iter.next();
			if (li instanceof JingleListener.Transport) {
				JingleListener.Transport mli = (JingleListener.Transport) li;
				mli.transportEstablished(local, remote);
			}
		}
	}

	/**
	 * Trigger a media closed event.
	 * 
	 * @param currPt current payload type that is cancelled.
	 */
	protected void triggerTransportClosed(final TransportCandidate cand) {
		ArrayList listeners = getListenersList();
		Iterator iter = listeners.iterator();
		while (iter.hasNext()) {
			JingleListener li = (JingleListener) iter.next();
			if (li instanceof JingleListener.Transport) {
				JingleListener.Transport mli = (JingleListener.Transport) li;
				mli.transportClosed(cand);
			}
		}
	}

	// States

	/**
	 * First stage when we send a session request.
	 */
	public class Inviting extends JingleNegotiator.State {

		public Inviting(final TransportNegotiator neg) {
			super(neg);
		}

		/**
		 * Create an initial Jingle packet with an empty transport.
		 */
		public Jingle eventInvite() {
			return new Jingle(getJingleTransport(null));
		}

		/**
		 * We have received some candidates. This can happen _before_ the ACK
		 * has been recieved...
		 * 
		 * @see org.jivesoftware.smackx.jingle.JingleNegotiator.State#eventInfo(org.jivesoftware.smackx.packet.Jingle)
		 */
		public Jingle eventInfo(final Jingle jin) throws XMPPException {
			// Parse the Jingle and get any proposed transport candidates
			addRemoteCandidates(obtainCandidatesList(jin));

			// Wait for some time and check if we have a valid candidate to
			// use...
			delayedCheckBestCandidate(session, jin);

			return super.eventInfo(jin);
		}

		/**
		 * The other endpoint has partially accepted our invitation: start
		 * offering a list of candidates.
		 * 
		 * @return an IQ packet
		 * @throws XMPPException
		 */
		public Jingle eventAck(final IQ iq) throws XMPPException {
			sendTransportCandidatesOffer();
			setState(pending);
			return super.eventAck(iq);
		}
	}

	/**
	 * We are accepting connections. This is the starting state when we accept a
	 * connection...
	 */
	public class Accepting extends JingleNegotiator.State {
		public Accepting(final TransportNegotiator neg) {
			super(neg);
		}

		/**
		 * We have received an invitation. The packet will be ACKed by lower
		 * levels...
		 */
		public Jingle eventInitiate(final Jingle jin) throws XMPPException {
			// Parse the Jingle and get any proposed transport candidates
			addRemoteCandidates(obtainCandidatesList(jin));

			// Start offering candidates
			sendTransportCandidatesOffer();

			// All these candidates will be checked asyncronously. Wait for some
			// time and check if we have a valid candidate to use...
			delayedCheckBestCandidate(session, jin);

			// Set the next state
			setState(pending);

			return super.eventInitiate(jin);
		}
	}

	/**
	 * We are still receiving candidates
	 */
	public class Pending extends JingleNegotiator.State {

		public Pending(final TransportNegotiator neg) {
			super(neg);
		}

		/**
		 * One of our transport candidates has been accepted.
		 * 
		 * @param jin The input packet
		 * @return a Jingle packet
		 * @throws XMPPException an exception
		 * 
		 * @see org.jivesoftware.smackx.jingle.JingleNegotiator.State#eventAccept(org.jivesoftware.smackx.packet.Jingle)
		 */
		public Jingle eventAccept(final Jingle jin) throws XMPPException {
			Jingle response = null;

			// Parse the Jingle and get the accepted candidate
			ArrayList accepted = obtainCandidatesList(jin);
			if (!accepted.isEmpty()) {
				TransportCandidate cand = (TransportCandidate) accepted.get(0);

				setAcceptedLocalCandidate(cand);

				if (isEstablished()) {
					setState(active);
				}
			}
			return response;
		}

		/**
		 * We have received another remote transport candidates.
		 * 
		 * @see org.jivesoftware.smackx.jingle.JingleNegotiator.State#eventInfo(org.jivesoftware.smackx.packet.Jingle)
		 */
		public Jingle eventInfo(final Jingle jin) throws XMPPException {

			sendTransportCandidatesOffer();

			// Parse the Jingle and get any proposed transport candidates
			addRemoteCandidates(obtainCandidatesList(jin));

			// Wait for some time and check if we have a valid candidate to
			// use...
			delayedCheckBestCandidate(session, jin);

			return super.eventInfo(jin);
		}

		/**
		 * None of our transport candidates has been accepted...
		 * 
		 * @see org.jivesoftware.smackx.jingle.JingleNegotiator.State#eventDecline(org.jivesoftware.smackx.packet.Jingle)
		 */
		public Jingle eventDecline(final Jingle inJingle) throws JingleException {
			throw new JingleException("No common payload found.");
		}
	}

	/**
	 * "Active" state: we have an agreement about the codec...
	 */
	public class Active extends JingleNegotiator.State {

		public Active(final TransportNegotiator neg) {
			super(neg);
		}

		/**
		 * We have an agreement.
		 * 
		 * @see org.jivesoftware.smackx.jingle.JingleNegotiator.State#eventEnter()
		 */
		public void eventEnter() {
			triggerTransportEstablished(getAcceptedLocalCandidate(),
					getBestRemoteCandidate());
			super.eventEnter();
		}

		/**
		 * We have finished the transport.
		 * 
		 * @see org.jivesoftware.smackx.jingle.JingleNegotiator.State#eventEnter()
		 */
		public void eventExit() {
			triggerTransportClosed(null);
			super.eventExit();
		}
	}

	// Subclasses

	/**
	 * Raw-UDP transport negotiator
	 * 
	 * @author Alvaro Saurin <alvaro.saurin@gmail.com>
	 */
	public static class RawUdp extends TransportNegotiator {

		/**
		 * Default constructor, with a JingleSession and transport manager.
		 * 
		 * @param js The Jingle session this negotiation belongs to.
		 * @param res The transport resolver to use.
		 */
		public RawUdp(final JingleSession js, final TransportResolver res) {
			super(js, res);
		}

		/**
		 * Get a JingleTransport instance.
		 */
		protected JingleTransport getJingleTransport(final TransportCandidate bestRemote) {
			JingleTransport.RawUdp jt = new JingleTransport.RawUdp();
			jt.addCandidate(new JingleTransport.RawUdp.Candidate(bestRemote));
			return jt;
		}

		/**
		 * Obtain the best common transport candidate obtained in the
		 * negotiation.
		 * 
		 * @return the bestRemoteCandidate
		 */
		public TransportCandidate getBestRemoteCandidate() {
			// Hopefully, we only have one validRemoteCandidate
			ArrayList cands = getValidRemoteCandidatesList();
			if (!cands.isEmpty()) {
				return (TransportCandidate) cands.get(0);
			} else {
				return null;
			}
		}

		/**
		 * Return true for fixed candidates.
		 */
		protected boolean acceptableTransportCandidate(final TransportCandidate tc) {
			return tc instanceof TransportCandidate.Fixed;
		}
	}

	/**
	 * Ice transport negotiator.
	 * 
	 * @author Alvaro Saurin <alvaro.saurin@gmail.com>
	 */
	public static class Ice extends TransportNegotiator {

		/**
		 * Default constructor, with a JingleSession and transport manager.
		 * 
		 * @param js The Jingle session this negotiation belongs to.
		 * @param res The transport manager to use.
		 */
		public Ice(final JingleSession js, final TransportResolver res) {
			super(js, res);
		}

		/**
		 * Get a JingleTransport instance.
		 * 
		 * @param bestRemote
		 */
		protected JingleTransport getJingleTransport(final TransportCandidate bestRemote) {
			JingleTransport.Ice jt = new JingleTransport.Ice();
			jt.addCandidate(new JingleTransport.Ice.Candidate(bestRemote));
			return jt;
		}

		/**
		 * Obtain the best remote candidate obtained in the negotiation so far.
		 * 
		 * @return the bestRemoteCandidate
		 */
		public TransportCandidate getBestRemoteCandidate() {
			TransportCandidate.Ice result = null;

			ArrayList cands = getValidRemoteCandidatesList();
			if (!cands.isEmpty()) {
				Collections.sort(cands);
				// Return the last candidate
				result = (TransportCandidate.Ice) cands.get(cands.size() - 1);
			}

			return result;
		}

		/**
		 * Return true for ICE candidates.
		 */
		protected boolean acceptableTransportCandidate(final TransportCandidate tc) {
			return tc instanceof TransportCandidate.Ice;
		}
	}
}
