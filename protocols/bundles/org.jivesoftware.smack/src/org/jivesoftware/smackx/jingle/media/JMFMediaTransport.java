package org.jivesoftware.smackx.jingle.media;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import javax.media.protocol.DataSource;
import javax.media.rtp.RTPManager;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.SendStream;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.event.NewReceiveStreamEvent;
import javax.media.rtp.event.ReceiveStreamEvent;

import org.jivesoftware.smackx.jingle.media.util.StreamConverter;
import org.jivesoftware.smackx.nat.TransportCandidate;
import org.jivesoftware.smackx.nat.TransportCandidate.Protocol;

/**
 * JMF based subclass of the abstract MediaTransport class. This class presently only supports
 * sending via UDP.
 * 
 * @author Alasdair North
 *
 */
public class JMFMediaTransport extends MediaTransport implements ReceiveStreamListener {
	
	private RTPManager rtpManager;
	private int receivingState, transmittingState;
	private SendStream sendStream;
	private ReceiveStream receiveStream;
	private InputStream inputStream, outputStream;
	private DataSource input;

	public JMFMediaTransport(TransportCandidate remoteCandidate, TransportCandidate localCandidate) {
		super(remoteCandidate, localCandidate);
		rtpManager = RTPManager.newInstance();
		transmittingState = MediaTransport.TRANSMITTER_INSTANTIATED;
		receivingState = MediaTransport.RECEIVER_INSTANTIATED;
	}

	public void configure() {
		if(getInput() == null) throw new UnableToConfigure("Tried to configure with input not set.");
		
		try {
			SessionAddress localAddr = new SessionAddress(InetAddress.getByName(
					getLocalCandidate().getIP()), getLocalCandidate().getPort());
			rtpManager.initialize(localAddr);
			rtpManager.addTarget(new SessionAddress(InetAddress.getByName(
					getRemoteCandidate().getIP()), getRemoteCandidate().getPort()));
			
			transmittingState = MediaTransport.TRANSMITTER_CONFIGURED;
			receivingState = MediaTransport.RECEIVER_CONFIGURED;
		} catch (Exception e) {
			throw new UnableToConfigure(e);
		}	
	}

	public void start() {
		//start listening for incoming streams
		rtpManager.addReceiveStreamListener(this);
		receivingState = MediaTransport.RECEIVER_WAITING;
		
		//and start sending
		try {
			sendStream = rtpManager.createSendStream(input, 0);
			sendStream.start();
			transmittingState = MediaTransport.TRANSMITTER_SENDING;
			
		} catch (Exception e) {
			throw new UnableToStart(e);
		}
	}

	public void stop() {
		try {
			sendStream.stop();
			transmittingState = MediaTransport.TRANSMITTER_CONFIGURED;
			rtpManager.removeReceiveStreamListener(this);
			receivingState = MediaTransport.RECEIVER_CONFIGURED;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void close() {
		try {
			sendStream.stop();
			sendStream = null;
			receiveStream = null;
			rtpManager.dispose();
			rtpManager = null;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getTransmitterState() {
		return transmittingState;
	}

	public int getReceiverState() {
		return receivingState;
	}

	public void update(ReceiveStreamEvent evt) {
		if (evt instanceof NewReceiveStreamEvent && receiveStream ==null) {
			
			try {
				receiveStream = ((NewReceiveStreamEvent)evt).getReceiveStream();
				receivingState = MediaTransport.RECEIVER_RECEIVING;
				notifyOutputListenersOutputReady(getOutput());
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//TODO deal with disconnection
		
	}

	public InputStream getOutput() {
		if(receiveStream == null) return null;
		if(outputStream == null) outputStream = StreamConverter.toAudioInputStream(receiveStream.getDataSource());
		
		return outputStream;
	}

	public Set getSupportedProtocols() {
		Set result = new HashSet();
		
		result.add(Protocol.UDP);
		
		return null;
	}

	public InputStream getInput() {
		return inputStream;
	}

	public void setInput(InputStream input) {
		this.input = StreamConverter.toDataSource(input);
		inputStream = input;
		
	}
}

