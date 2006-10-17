package org.jivesoftware.smackx.jingle;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.nat.TransportCandidate;

/**
 * Jingle listeners.
 * 
 * </p>
 * 
 * This is the list of events that can be observed from a JingleSession and some
 * sub negotiators. This listeners can be added to different elements of the
 * Jingle model.
 * 
 * </p>
 * 
 * For example, a JingleManager can notify any JingleListener.SessionRequest
 * listener when a new session request is received. In this case, the
 * <i>sessionRequested()</i> of the listener will be executed, and the listener
 * will be able to <i>accept()</i> or <i>decline()</i> the invitation.
 * 
 * </p>
 * 
 * @author Alvaro Saurin
 */
public interface JingleListener {

	/**
	 * Jingle session request listener.
	 * 
	 * @author Alvaro Saurin
	 */
	public static interface SessionRequest extends JingleListener {
		/**
		 * A request to start a session has been recieved from another user.
		 * 
		 * @param request The request from the other user.
		 */
		public void sessionRequested(final JingleSessionRequest request);
	}

	/**
	 * Interface for listening for session events.
	 */
	public static interface Session extends JingleListener {
		/**
		 * Notification that the session has been established. Arguments specify
		 * the payload type and transport to use.
		 * 
		 * @param pt The Payload tyep to use
		 * @param rc The remote candidate to use for connecting to the remote
		 *            service.
		 * @param lc The local candidate where we must listen for connections
		 */
		public void sessionEstablished(final PayloadType pt, final TransportCandidate rc,
				final TransportCandidate lc);

		/**
		 * Notification that the session was declined.
		 * 
		 * @param reason The reason (if any).
		 */
		public void sessionDeclined(final String reason);

		/**
		 * Notification that the session was redirected.
		 */
		public void sessionRedirected(final String redirection);

		/**
		 * Notification that the session was closed normally.
		 * 
		 * @param reason The reason (if any).
		 */
		public void sessionClosed(final String reason);

		/**
		 * Notification that the session was closed due to an exception.
		 * 
		 * @param e the exception.
		 */
		public void sessionClosedOnError(final XMPPException e);
	}

	/**
	 * Interface for listening to transport events.
	 */
	public static interface Transport extends JingleListener {
		/**
		 * Notification that the transport has been established.
		 * 
		 * @param local The transport candidate that has been used for listening
		 *            in the local machine
		 * @param remote The transport candidate that has been used for
		 *            transmitting to the remote machine
		 */
		public void transportEstablished(final TransportCandidate local,
				final TransportCandidate remote);

		/**
		 * Notification that a transport must be cancelled.
		 * 
		 * @param cand The transport candidate that must be cancelled. A value
		 *            of "null" means all the transports for this session.
		 */
		public void transportClosed(final TransportCandidate cand);

		/**
		 * Notification that the transport was closed due to an exception.
		 * 
		 * @param e the exception.
		 */
		public void transportClosedOnError(final XMPPException e);
	}

	/**
	 * Interface for listening to media events.
	 */
	public static interface Media extends JingleListener {
		/**
		 * Notification that the media has been negotiated and established.
		 * 
		 * @param pt The payload type agreed.
		 */
		public void mediaEstablished(final PayloadType pt);

		/**
		 * Notification that a payload type must be cancelled
		 * 
		 * @param cand The payload type that must be closed
		 */
		public void mediaClosed(final PayloadType cand);
	}

	/**
	 * Interface for listening to media info events.
	 */
	public static interface MediaInfo extends JingleListener {
		/**
		 * The other end is busy.
		 */
		public void mediaInfoBusy();

		/**
		 * We are on hold.
		 */
		public void mediaInfoHold();

		/**
		 * The media is muted.
		 */
		public void mediaInfoMute();

		/**
		 * We are queued.
		 */
		public void mediaInfoQueued();

		/**
		 * We are ringing.
		 */
		public void mediaInfoRinging();
	}
}
