package org.jivesoftware.smackx.jingle;

import java.util.ArrayList;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smackx.packet.Jingle;
import org.jivesoftware.smackx.packet.JingleError;

/**
 * Basic Jingle negotiator.
 * 
 * </p>
 * 
 * JingleNegotiator implements some basic behavior for every Jingle negotiation.
 * It implements a "state" pattern: each stage should process Jingle packets and
 * act depending on the current state in the negotiation...
 * 
 * </p>
 * 
 * @author Alvaro Saurin
 */
public abstract class JingleNegotiator {

	private State state; // Current negotiation state

	private XMPPConnection connection; // The connection associated

	private final ArrayList listeners = new ArrayList();

	private String expectedAckId;

	/**
	 * Default constructor.
	 */
	public JingleNegotiator() {
		this(null);
	}

	/**
	 * Default constructor with a XMPPConnection
	 * 
	 * @param connection the connection associated
	 */
	public JingleNegotiator(final XMPPConnection connection) {
		this.connection = connection;
		state = null;
	}

	/**
	 * Get the XMPP connection associated with this negotiation.
	 * 
	 * @return the connection
	 */
	public XMPPConnection getConnection() {
		return connection;
	}

	/**
	 * Set the XMPP connection associated.
	 * 
	 * @param connection the connection to set
	 */
	public void setConnection(final XMPPConnection connection) {
		this.connection = connection;
	}

	/**
	 * Inform if current state is null
	 * 
	 * @return true if current state is null
	 */
	public boolean invalidState() {
		return state == null;
	}

	/**
	 * Return the current state
	 * 
	 * @return the state
	 */
	public State getState() {
		return state;
	}

	/**
	 * Return the current state class
	 * 
	 * @return the state
	 */
	public Class getStateClass() {
		if (state != null) {
			return state.getClass();
		} else {
			return Object.class;
		}
	}

	/**
	 * Set the new state.
	 * 
	 * @param state the state to set
	 * @throws XMPPException
	 */
	protected void setState(final State newState) {
		boolean transition = newState != state;

		if (transition && state != null) {
			state.eventExit();
		}

		state = newState;

		if (transition && state != null) {
			state.eventEnter();
		}
	}

	// Acks management

	public void addExpectedId(final String id) {
		expectedAckId = id;
	}

	public boolean isExpectedId(final String id) {
		if (id != null) {
			return id.equals(expectedAckId);
		} else {
			return false;
		}
	}

	public void removeExpectedId(final String id) {
		addExpectedId((String) null);
	}

	// Listeners

	/**
	 * Add a Jingle session listener to listen to incoming session requests.
	 * 
	 * @param li The listener
	 * 
	 * @see JingleListener
	 */
	public void addListener(final JingleListener li) {
		synchronized (listeners) {
			listeners.add(li);
		}
	}

	/**
	 * Removes a Jingle session listener.
	 * 
	 * @param li The jingle session listener to be removed
	 * @see JingleListener
	 */
	public void removeListener(final JingleListener li) {
		synchronized (listeners) {
			listeners.remove(li);
		}
	}

	/**
	 * Get a copy of the listeners
	 * 
	 * @return a copy of the listeners
	 */
	protected ArrayList getListenersList() {
		ArrayList result;

		synchronized (listeners) {
			result = new ArrayList(listeners);
		}

		return result;
	}

	/**
	 * Dispatch an incomming packet. This method is responsible for recognizing
	 * the packet type and, depending on the current state, deliverying the
	 * packet to the right event handler and wait for a response.
	 * 
	 * @param iq the packet received
	 * @param id the ID of the response that will be sent
	 * @return the new packet to send (either a Jingle or an IQ error).
	 * @throws XMPPException
	 */
	public abstract IQ dispatchIncomingPacket(final IQ iq, final String id)
			throws XMPPException;

	/**
	 * Close the negotiation.
	 */
	public void close() {
		setState(null);
	}

	/**
	 * A Jingle exception.
	 * 
	 * @author Alvaro Saurin <alvaro.saurin@gmail.com>
	 */
	public static class JingleException extends XMPPException {

		private final JingleError error;

		/**
		 * Default constructor.
		 */
		public JingleException() {
			super();
			error = null;
		}

		/**
		 * Constructor with an error message.
		 * 
		 * @param error The message.
		 */
		public JingleException(final String msg) {
			super(msg);
			error = null;
		}

		/**
		 * Constructor with an error response.
		 * 
		 * @param error The error message.
		 */
		public JingleException(final JingleError error) {
			super();
			this.error = error;
		}

		/**
		 * Return the error message.
		 * 
		 * @return the error
		 */
		public JingleError getError() {
			return error;
		}
	}

	/**
	 * Negotiation state and events.
	 * 
	 * </p>
	 * 
	 * Describes the negotiation stage.
	 */
	public static class State {

		private JingleNegotiator neg; // The negotiator

		/**
		 * Default constructor, with a reference to the negotiator.
		 * 
		 * @param neg The negotiator instance.
		 */
		public State(final JingleNegotiator neg) {
			this.neg = neg;
		}

		/**
		 * Get the negotiator
		 * 
		 * @return the negotiator.
		 */
		public JingleNegotiator getNegotiator() {
			return neg;
		}

		/**
		 * Set the negotiator.
		 * 
		 * @param neg the neg to set
		 */
		public void setNegotiator(final JingleNegotiator neg) {
			this.neg = neg;
		}

		// State transition events

		public Jingle eventAck(final IQ iq) throws XMPPException {
			// We have received an Ack
			return null;
		}

		public void eventError(final IQ iq) throws XMPPException {
			throw new JingleException(iq.getError().getMessage());
		}

		public Jingle eventInvite() throws XMPPException {
			throw new IllegalStateException(
					"Negotiation can not be started in this state.");
		}

		public Jingle eventInitiate(final Jingle jin) throws XMPPException {
			return null;
		}

		public Jingle eventAccept(final Jingle jin) throws XMPPException {
			return null;
		}

		public Jingle eventRedirect(final Jingle jin) throws XMPPException {
			return null;
		}

		public Jingle eventModify(final Jingle jin) throws XMPPException {
			return null;
		}

		public Jingle eventDecline(final Jingle jin) throws XMPPException {
			return null;
		}

		public Jingle eventInfo(final Jingle jin) throws XMPPException {
			return null;
		}

		public Jingle eventTerminate(final Jingle jin) throws XMPPException {
			if (neg != null) {
				neg.close();
			}
			return null;
		}

		public void eventEnter() {
		}

		public void eventExit() {
			if (neg != null) {
				neg.removeExpectedId(null);
			}
		}
	}
}
