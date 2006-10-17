package org.jivesoftware.smackx.jingle.media;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import java.io.InputStream;

import org.jivesoftware.smackx.nat.TransportCandidate;

/**
 * Subclasses of MediaTransport are responisble for the transmission of data, implementing
 * TransportCandidates negotiated by the TransportNegotiator.
 * 
 * @author Alasdair North
 *
 */
public abstract class MediaTransport {
	
	public static final int TRANSMITTER_INSTANTIATED = 0;
	public static final int TRANSMITTER_CONFIGURED = 1;
	public static final int TRANSMITTER_SENDING = 2;
	public static final int TRANSMITTER_CLOSED = 3;
	
	
	public static final int RECEIVER_INSTANTIATED = 7;
	public static final int RECEIVER_CONFIGURED = 8;
	public static final int RECEIVER_WAITING = 9;
	public static final int RECEIVER_RECEIVING = 10;
	public static final int RECEIVER_CLOSED = 11;
	
	private TransportCandidate remoteCandidate, localCandidate;
	private Set outputListeners;
	
	/**
	 * This empty constructor is needed by the SPI, it shouldn't do anything other than create an
	 * object.
	 */
	public MediaTransport(){}
	
	/**
	 * Create a new MediaTransport to use the transport method specified by the successful
	 * TransportCandidate.
	 * @param remoteCandidate The remote candidate to use for connecting to the remote service.
	 * @param localCandidate The local candidate where we must listen for connections
	 */
	public MediaTransport(TransportCandidate remoteCandidate, TransportCandidate localCandidate) {
		this.localCandidate = localCandidate;
		this.remoteCandidate = remoteCandidate;
		outputListeners = new HashSet();
	}
	
	/**
	 * Called to initiate the transition from the INSTANTIATED states to CONFIGURED states.
	 */
	public abstract void configure();
	
	/**
	 * Initiates transition from TRANSMITTER_CONFIGURED to TRANSMITTER_SENDING and
	 * RECEIVER_CONFIGURED to RECEIVER_WAITING. The MediaTransport will start transmitting data
	 * from the input and will begin waiting for data transmitted from the other end.
	 */
	public abstract void start();
	
	/**
	 * Initiates transition from TRANSMITTER_SENDING to TRANSMITTER_CONFIGURED and
	 * RECEIVER_WAITING to RECEIVER_CONFIGURED. The transmitter stops transmitting and the
	 * receiver stops receiving and also stops listening for data transmitted from the other end.
	 */
	public abstract void stop();
	
	/**
	 * Releases all resources held by the MediaTransport and cease's its activity. Will return it
	 * to the INSTANTIATED states (i.e. it must be reconfigured before being used again).
	 */
	public abstract void close();
	
	/**
	 * @return the state of the transmission side of the MediaTransport.
	 */
	public abstract int getTransmitterState();
	
	/**
	 * @return the state of the receiving side of the MediaTransport.
	 */
	public abstract int getReceiverState();
	
	/**
	 * @return a Set of the TransportCandidate.Protocols supported by this MediaTransport.
	 */
	public abstract Set getSupportedProtocols();
	
	public TransportCandidate getLocalCandidate() {
		return localCandidate;
	}

	public TransportCandidate getRemoteCandidate() {
		return remoteCandidate;
	}
	
	/**
	 * @return the DataSource from which this MediaTransport gets its data.
	 */
	public abstract InputStream getInput();

	/**
	 * This method must be called before the MediaTransport is started.
	 * @param input the DataSource from which this MediaTransport will get its data.
	 */
	public abstract void setInput(InputStream input);
	
	/**
	 * This method may return nothing but null until the MediaTransport is in the RECEIVER_RECEIVING
	 * state.
	 * @return the data received bt the MediaTransport.
	 */
	public abstract InputStream getOutput();

	/**
	 * Add an output listener to the MediaTransport. This listener will be notified when the 
	 * MediaTransport receives a new data stream.
	 * @param listener the listener to be added.
	 */
	public void addOutputListener(JingleMediaListener.Output listener) {
		outputListeners.add(listener);
	}

	/**
	 * Add an output listener to the MediaTransport. This listener will no longer be notified
	 * of new received data streams.
	 * @param listener the listener to be removed.
	 */
	public void removeOutputListener(JingleMediaListener.Output listener) {
		outputListeners.remove(listener);
	}
	
	/**
	 * Notify the output listeners that a new output is available.
	 * @param output new output DataSource.
	 */
	protected void notifyOutputListenersOutputReady(InputStream output) {
		Iterator it = outputListeners.iterator();
		while(it.hasNext()) ((JingleMediaListener.Output) it.next()).outputChanged(output);
	}
	
	/**
	 * @param tc
	 * @return the Protocol to be used for implementing the given TransportCandidate.
	 */
	public static TransportCandidate.Protocol getProtocol(TransportCandidate tc) {
		if(tc instanceof TransportCandidate.Ice) return ((TransportCandidate.Ice) tc).getProto();
		else return TransportCandidate.Protocol.UDP;
	}
	
	public static class UnableToConfigure extends Error {
		private static final long serialVersionUID = 1L;
		public UnableToConfigure(Throwable cause) {
			super(cause);
		}
		public UnableToConfigure(String cause) {
			super(cause);
		}
	}
	
	public static class UnableToStart extends Error {
		private static final long serialVersionUID = 1L;
		public UnableToStart(Throwable cause) {
			super(cause);
		}
	}
}

