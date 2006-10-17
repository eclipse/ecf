package org.jivesoftware.smackx.jingle.media;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jivesoftware.smackx.jingle.PayloadType;
import org.jivesoftware.smackx.jingle.media.JingleMediaManager.ContentType;

/**
 * MediaEngines encode and decode data.
 * 
 * @author Alasdair North
 */
public abstract class MediaEngine {

	public static final int INSTANTIATED = 0;
	public static final int CONFIGURING = 1;
	public static final int CONFIGURED = 2;
	public static final int STARTED = 3;
	public static final int CLOSED = 4;
	
	private static final OutputMonitoringThread monitorThread = new OutputMonitoringThread();
	
	private PayloadType encodingFormat;
	private Set outputListeners;
	
	
	/**
	 * This empty constructor is needed by the SPI, it doesn't do anything other than create an
	 * object.
	 */
	public MediaEngine() {}
	
	/**
	 * Create a new MediaEngine.
	 * @param encodedFormat The Format that encoded data dealt with by this MediaEngine is in.
	 */
	public MediaEngine(PayloadType encodedFormat) {
		this.encodingFormat = encodedFormat;
		outputListeners = new HashSet();
	}

	/**
	 * @return The InputStream from which this MediaEngine gets its data.
	 */
	public abstract InputStream getInput();

	/**
	 * Set the MediaEngine's input, this should be done before configuring the MediaEngine or 
	 * calling one of the getSupportedPayloadTypes methods.
	 * @param input Set the DataSource from which this MediaEngine gets its data.
	 */
	public abstract void setInput(InputStream input);

	/**
	 * This method does not need to return anything other than null until the MediaEngine is 
	 * STARTED. When it changes output listeners will be notified of the new output DataSource.
	 * @return the DataSource representing the output of this MediaEngine.
	 */
	public abstract InputStream getOutput();
	
	/**
	 * @return the PayloadType of encoded data.
	 */
	public synchronized PayloadType getPayloadType() {
		return encodingFormat;
	}
	
	/**
	 * Get the current state of the MediaEngine. This will be one of the constants specified in
	 * MediaEngine.
	 * @return the MediaEngine's current state.
	 */
	public abstract int getState();
	
	/**
	 * Called to intitiate the transition from the INSTANTIATED state to the CONFIGURING state, and
	 * then finally to CONFIGURED.
	 * @throws UnableToConfigureMediaEngine when unable to configure.
	 */
	public abstract void configure() throws UnableToConfigureMediaEngine;
	
	/**
	 * Starts the transition from CONFIGURED to STARTED. When this is called the MediaEngine 
	 * should start to process data and output it.
	 */
	public abstract void start();
	
	/**
	 * Starts the transition from STARTED back to CONFIGURED. When this is called the MediaEngine
	 * should stop sending data to the output. 
	 */
	public abstract void stop();
	
	/**
	 * Releases all the resources held by the MediaEngine and ceases it's activity. This will put
	 * it back to the INSTANTIATED state. In order to be used again it will have to be configured
	 * again.
	 */
	public abstract void close();
	
	public abstract Set getSupportedPayloadTypes(ContentType contentType);
	
	/**
	 * Add an output listener to the MediaEngine. This listener will be notified when the output
	 * changes to anything other than null.
	 * @param listener the listener to be added.
	 */
	public void addOutputListener(JingleMediaListener.Output listener) {
		outputListeners.add(listener);
		if(!monitorThread.isStarted()) monitorThread.setStarted(true);
		if(!monitorThread.isInMonitoredList(this)) monitorThread.addToMonitoredList(this);
	}

	/**
	 * Remove an output listener from the MediaEngine. This listener will no longer receive
	 * notifications of changes in output. 
	 * @param listener the listener to be removed.
	 */
	public void removeOutputListener(JingleMediaListener.Output listener) {
		outputListeners.remove(listener);
		if(outputListeners.isEmpty()) monitorThread.removeFromMonitoredList(this);
		if(monitorThread.isMonitoredEmpty()) monitorThread.setStarted(false);
	}
	
	private void notifyOutputListenersOutputReady(InputStream output) {
		Iterator it = outputListeners.iterator();
		while(it.hasNext()) ((JingleMediaListener.Output) it.next()).outputChanged(output);
	}
	
	/**
	 * Subclasses of MediaEngine.Encoding are responsible for encoding raw data to be transmitted
	 * by MediaTransport classes. In order to be used by the jingle media code they must have 
	 * entries in the META-INF/services directory of a jar file in the class path. They must also
	 * have a zero argument constructor.
	 * 
	 * @author Alasdair North
	 */	
	public static abstract class Encoding extends MediaEngine {
		
		public Encoding(PayloadType outputFormat) {
			super(outputFormat);
		}
		
		/**
		 * Return a set of the PayloadTypes that this class can encode data to for the given ContentType.
		 * These ContentTypes are specified using the constants in JingleMediaManager. The MediaEngine's
		 * input must be specified before calling this method.
		 * @param contentType 
		 * @return Set of supported PayloadTypes
		 */
		public abstract Set getSupportedPayloadTypes(ContentType contentType);
	}
	
	/**
	 * Subclasses of MediaEngine.Decoding are responsible for decoding encoded data received
	 * by MediaTransport classes. In order to be used by the jingle media code they must have 
	 * entries in the META-INF/services directory of a jar file in the class path. They must also
	 * have a zero argument constructor.
	 * 
	 * @author Alasdair North
	 */
	public static abstract class Decoding extends MediaEngine {
		
		public Decoding(PayloadType inputFormat) {
			super(inputFormat);
		}
		
		/**
		 * Return a set of the PayloadTypes that this class can decode data from for the given ContentType.
		 * These ContentTypes are specified using the constants in JingleMediaManager. The MediaEngine's
		 * input does not need to be specified before calling this method.
		 * @param contentType 
		 * @return Set of supported PayloadTypes
		 */
		public abstract Set getSupportedPayloadTypes(ContentType contentType);
	}
	
	public static class StateOrderViolationError extends Error {
		private static final long serialVersionUID = 1L;
		
		public StateOrderViolationError(String text) {
			super(text);
		}
		
	}
	
	public static class InputNotSetError extends Error {
		private static final long serialVersionUID = 1L;
		
		public InputNotSetError(String text) {
			super(text);
		}
	}
	
	public static class UnableToConfigureMediaEngine extends Exception {
		private static final long serialVersionUID = 1L;

		public UnableToConfigureMediaEngine(Throwable cause) {
			super(cause);
		}
		
		public UnableToConfigureMediaEngine(String cause) {
			super(cause);
		}
	}
	
	/**
	 * A thread to monitor the outputs of MediaEngines and notify output listeners of changes.
	 * @author Alasdair North
	 */
	private static class OutputMonitoringThread extends Thread {
		
		private volatile boolean started = false;
//		Map from MediaEngines to DataSources used to monitor for changes in output
		private volatile Map monitored = new HashMap();
		
		public boolean isStarted() {
			return started;
		}
		
		public void setStarted(boolean started) {
			this.started = started;
			if(started) super.start();
		}
		
		public void addToMonitoredList(MediaEngine me) {
			synchronized(monitored) {
				monitored.put(me, null);
			}
		}
		
		public void removeFromMonitoredList(MediaEngine me) {
			synchronized(monitored) {
				monitored.remove(me);
			}
		}
		
		public boolean isInMonitoredList(MediaEngine me) {
			synchronized(monitored) {
				return monitored.containsKey(me);
			}
		}
		
		public boolean isMonitoredEmpty() {
			synchronized(monitored) {
				return monitored.isEmpty();
			}
		}
		
		public void run() {
			
			while(started) {
				synchronized(monitored) {
					Set keySet = new HashSet(monitored.keySet());
					Iterator it = keySet.iterator();
					while(it.hasNext()) {
						
						MediaEngine currentKey = (MediaEngine) it.next();
						InputStream keysOutput;
						try {
							keysOutput = currentKey.getOutput();
						} catch (Exception e) {
							keysOutput = null;
						}
						
//						should listener be changed to notify of changes in output and notify of null
//						as well?
						if(keysOutput != monitored.get(currentKey) && keysOutput != null)
							currentKey.notifyOutputListenersOutputReady(keysOutput);
						
						monitored.put(currentKey, keysOutput);
					}
				}
			}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {}
		}
	}

}

