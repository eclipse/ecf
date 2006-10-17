package org.jivesoftware.smackx.jingle.media;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.sound.sampled.AudioInputStream;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.jingle.JingleListener;
import org.jivesoftware.smackx.jingle.JingleSession;
import org.jivesoftware.smackx.jingle.PayloadType;
import org.jivesoftware.smackx.jingle.media.JingleMediaListener.Output;
import org.jivesoftware.smackx.jingle.media.MediaEngine.UnableToConfigureMediaEngine;
import org.jivesoftware.smackx.nat.TransportCandidate;

/**
 * Class representing ongoing peer-to-peer connections negotiated via Jingle.
 * 
 * @author Alasdair North
 *
 */
public class JingleMediaSession implements JingleListener.Session {

	private JingleSession jingleSession;
	private AudioInputStream input;
	private JingleMediaManager mediaManager;
	private Set outputListeners;
	
	private MediaEngine.Encoding encodingMediaEngine;
	private MediaEngine.Decoding decodingMediaEngine;
	private MediaTransport mediaTransport;
	
	/**
	 * Create a new JingleMediaSession from the given JingleSession.
	 * @param input the DataSource from which data will be taken.
	 * @param jingleSession the JingleSession object used to negotiate session parameters.
	 * @param mediaManager the JingleMediaManager that created this JingleMediaSession.
	 */
	public JingleMediaSession(AudioInputStream input, JingleSession jingleSession, JingleMediaManager mediaManager) {
		this.input = input;
		this.jingleSession = jingleSession;
		this.mediaManager = mediaManager;
		
		outputListeners = new HashSet();
		
		jingleSession.addListener(this);
	}
	
	/**
	 * This method will return null until data starts being received.
	 * @return data received and decoded by the JingleMediaSession.
	 */
	public InputStream getOutput() {
		if(decodingMediaEngine == null) return null;
		return decodingMediaEngine.getOutput();
	}

	/**
	 * Release all resources held by the JingleMediaSession and cease operation.
	 */
	public void close() {
		jingleSession.close();
		jingleSession = null;
		encodingMediaEngine.close();
		encodingMediaEngine = null;
		decodingMediaEngine.close();
		decodingMediaEngine = null;
		mediaTransport.close();
		mediaTransport = null;
	}
	
	private void notifyOutputListeners(InputStream output) {
		Iterator it = outputListeners.iterator();
		while(it.hasNext()) ((JingleMediaListener.Output) it.next()).outputChanged(output);
	}
	
	/**
	 * Add an output listener to be notified when the JingleMediaSessions output changes.
	 * @param listener the listener to be added.
	 */
	public void addOutputListener(Output listener) {
		outputListeners.add(listener);
	}

	/**
	 * Remove an output listener. This listener will no longer be notified when the
	 * JingleMediaSessions output changes.
	 * @param listener the listner to be removed.
	 */
	public void removeOutputListener(Output listener) {
		outputListeners.remove(listener);
	}
	
	/**
	 * Add a JingleListener to be notified of JingleSession events.
	 * @param listener the listener to be added.
	 */
	public void addJingleListener(JingleListener listener) {
		if(listener instanceof Session) jingleSession.addListener(listener);
		if(listener instanceof Media) jingleSession.addMediaListener((Media)listener);
		if(listener instanceof Transport)
			jingleSession.addTransportListener((Transport)listener);			
	}
	
	/**
	 * Remove a JingleListener. This listener will no longer be notified of JingleSession events.
	 * @param listener the listner to be removed.
	 */
	public void removeJingleListener(JingleListener listener) {
		if(listener instanceof Session) jingleSession.removeListener(listener);
		if(listener instanceof Media) jingleSession.removeMediaListener((Media)listener);
		if(listener instanceof Transport)
			jingleSession.removeTransportListener((Transport)listener);	
	}
	
//	JingleListener.Session methods  --------------------------------------------------------
	
	public void sessionEstablished() {
		// TODO Should anything be done here?
	}

	public void sessionDeclined() {
		// TODO Should anything be done here?
	}

	public void sessionRedirected(String redirection) {
		// TODO Start a new JingleMediaSession?
	}

	public void sessionClosed() {
		close();
	}

	public void sessionClosedOnError(Exception e) {
		close();		
	}
	
	public void sessionDeclined(String reason) {
		// TODO Auto-generated method stub
		
	}

	public void sessionClosed(String reason) {
		// TODO Auto-generated method stub
		
	}

	public void sessionClosedOnError(XMPPException e) {
		// TODO Auto-generated method stub
		
	}
	
	public void sessionEstablished(final PayloadType pt, final TransportCandidate rc, final TransportCandidate lc) {
		
		Thread mediaConnectionCreationThread = new Thread() {
			public void run() {
				
				encodingMediaEngine = mediaManager.getEncodingMediaEngine(pt, input);
				encodingMediaEngine.addOutputListener(new EncOutListener());
				try {
					encodingMediaEngine.configure();
				} catch (UnableToConfigureMediaEngine e1) {
					e1.printStackTrace();
				}
				
				//wait for the encoding engine to configure itself
				while(encodingMediaEngine.getState() != MediaEngine.CONFIGURED) {
					try {
						wait(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				encodingMediaEngine.start();
				
				mediaTransport = mediaManager.getMediaTransport(rc, lc);
				mediaTransport.addOutputListener(new TranOutListener());
				
				mediaTransport.setInput(encodingMediaEngine.getOutput());
				mediaTransport.configure();
				
//				wait for the transport to configure itself
				while(mediaTransport.getTransmitterState() != MediaTransport.TRANSMITTER_CONFIGURED ||
						mediaTransport.getReceiverState() != MediaTransport.RECEIVER_CONFIGURED) {
					try {
						wait(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				mediaTransport.start();
			}
		};
		
		mediaConnectionCreationThread.start();
		
	}
	
	private class EncOutListener implements JingleMediaListener.Output {

		public void outputChanged(InputStream output) {
			
			
		}
		
	}
	
	private class TranOutListener implements JingleMediaListener.Output {

		public void outputChanged(InputStream output) {
			
//			hook it up to a decoding media engine
			decodingMediaEngine = mediaManager.getDecodingMediaEngine(encodingMediaEngine.getPayloadType(), output);
			decodingMediaEngine.addOutputListener(new DecOutListener());
			
			try {
				decodingMediaEngine.configure();
			} catch (UnableToConfigureMediaEngine e1) {
				e1.printStackTrace();
			}
			
			while(decodingMediaEngine.getState() != MediaEngine.CONFIGURED) {
				try {
					wait(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			decodingMediaEngine.start();
		}
		
	}
	
	private class DecOutListener implements JingleMediaListener.Output {

		public void outputChanged(InputStream output) {
			notifyOutputListeners(output);
		}
		
	}

}

