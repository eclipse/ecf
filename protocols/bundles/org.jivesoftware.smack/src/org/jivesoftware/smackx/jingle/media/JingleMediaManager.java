package org.jivesoftware.smackx.jingle.media;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.sound.sampled.AudioSystem;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.jingle.JingleListener;
import org.jivesoftware.smackx.jingle.JingleManager;
import org.jivesoftware.smackx.jingle.JingleSession;
import org.jivesoftware.smackx.jingle.JingleSessionRequest;
import org.jivesoftware.smackx.jingle.PayloadType;
import org.jivesoftware.smackx.jingle.media.util.FormatTranslator;
import org.jivesoftware.smackx.nat.STUNResolver;
import org.jivesoftware.smackx.nat.TransportCandidate;
import org.jivesoftware.smackx.nat.TransportResolver;
import org.jivesoftware.smackx.nat.TransportCandidate.Protocol;

import sun.misc.Service;

/**
 * The JingleMediaManager is used to initiate peer-to-peer connections
 * using the Jingle protocol (JEP-0166).
 * 
 * @author Alasdair North
 *
 */
public class JingleMediaManager {
	
	private JingleManager jingleManager;
	private TransportSupportStore supportedTransports;
	
	private Class[] encodingMediaEngines, decodingMediaEngines, mediaTransports;
	
	//default lists to be used if none are found via the SPI
	private static final Class[] defaultEME = {EncodingJMFMediaEngine.class, EncodingJSMediaEngine.class};
	private static final Class[] defaultDME = {DecodingJMFMediaEngine.class, DecodingJSMediaEngine.class};
	private static final Class[] defaultMT = {JMFMediaTransport.class};
	
	private Comparator ptComparator;
	
	
	/**
	 * Create a new JingleMediaManager.
	 * @param xmppConnection XMPPConnection that sessions will be negotiated over.
	 * @param tr TransportResolver to be used. 
	 * @param ptComparator Comparator to be used when sorting PayloadTypes into order of preference.
	 */
	public JingleMediaManager(XMPPConnection xmppConnection, TransportResolver tr, Comparator ptComparator) {
		jingleManager = new JingleManager(xmppConnection, tr);
		JingleManager.setServiceEnabled(xmppConnection, true);
		
		this.ptComparator = ptComparator;
				
		//load the Classes for the media engines and media transports in the class path
		Iterator itEnc = Service.providers(MediaEngine.Encoding.class);
		encodingMediaEngines = getClassArray(itEnc);
		if(encodingMediaEngines.length == 0) encodingMediaEngines = defaultEME;
		
		Iterator itDec = Service.providers(MediaEngine.Decoding.class);
		decodingMediaEngines = getClassArray(itDec);
		if(decodingMediaEngines.length == 0) decodingMediaEngines = defaultDME;
		
		Iterator itTran = Service.providers(MediaTransport.class);
		mediaTransports = getClassArray(itTran);
		if(mediaTransports.length == 0) mediaTransports = defaultMT;
		
		initializeSupportedTransports();
	}
	
	/**
	 * Create a new JingleMediaManager using the default PayloadType comparator (which prefers low
	 * bandwidth solutions).
	 * @param xmppConnection XMPPConnection that sessions will be negotiated over.
	 * @param tr TransportResolver to be used. 
	 */
	public JingleMediaManager(XMPPConnection xmppConnection, TransportResolver tr) {
		this(xmppConnection, tr, null);
		setPTComparator(new DefaultPTC());
	}
	
	/**
	 * Create a new JingleMediaManager using the default PayloadType comparator (which prefers low
	 * bandwidth solutions) and TransportManager (which only negotiates UDP sessions).
	 * @param xmppConnection XMPPConnection that sessions will be negotiated over.
	 */
	public JingleMediaManager(XMPPConnection xmppConnection) {
		this(xmppConnection, new STUNResolver());
	}
	
	/**
	 * Creates a JingleMediaSession to start communication with another user.
	 * @param JID the identity of the other user.
	 * @param contentType the desired content type of the session.
	 * @param input the source of data for the session.
	 * @return a JingleMediaSession object which will negotiate and implement a Jingle session.
	 */
	public JingleMediaSession createOutgoingJingleSession(String JID, ContentType contentType, 
			InputStream input) {
		
		if(contentType.equals(ContentType.AUDIO)) {
		
			JingleSession newSession = jingleManager.createOutgoingJingleSession(JID,
					getSupportedPTs(contentType, input));
			try {
				newSession.start(null);
				return new JingleMediaSession(AudioSystem.getAudioInputStream(input), newSession, this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public JingleMediaSession createIncomingJingleSession(JingleSessionRequest request, ContentType contentType, InputStream input) {
		
		if(contentType.equals(ContentType.AUDIO)) {
		
			JingleSession js = jingleManager.createIncomingJingleSession(request, getSupportedPTs(contentType, input));
			
			try {
				js.start(null);
				return new JingleMediaSession(AudioSystem.getAudioInputStream(input), js, this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * Get a decoding MediaEngine for the specified PayloadType.
	 * @param pt type of input.
	 * @param input The InputStream containing the input for the MediaEngine.
	 * @return MediaEngine for decoding the given input.
	 */
	public MediaEngine.Decoding getDecodingMediaEngine(PayloadType pt, InputStream input) {
		
		return (MediaEngine.Decoding) getMediaEngine(false, pt, input);
	}
	
	/**
	 * Get an encoding MediaEngine for the specified PayloadType.
	 * @param pt type of output desired.
	 * @param input The InputStream containing the input for the MediaEngine. 
	 * @return MediaEngine for encoding the given input.
	 */
	public MediaEngine.Encoding getEncodingMediaEngine(PayloadType pt, InputStream input) {
		
		return (MediaEngine.Encoding) getMediaEngine(true, pt, input);
	}
	
	private MediaEngine getMediaEngine(boolean encoding, PayloadType pt, InputStream input) {
		
		Class meClass;
		EncodingSupportStore supportedPTs = generateSupportedAudioPTs(input);
		
		if(encoding) meClass = supportedPTs.getEncodingClassForPT(pt);
		else meClass = supportedPTs.getDecodingClassForPT(pt);
		
		if(meClass != null) {
			try {
			
				Object[] args = {pt};
				MediaEngine me = (MediaEngine) getInstance(meClass, args);
				me.setInput(input);
				return me;
			
			} catch (Exception e) {
				// in this case there was something wrong with the instantiation
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}
	
	public List getSupportedPTs(ContentType contentType, InputStream input) {
		
		if(contentType.equals(ContentType.AUDIO)) {
			
			Set setPTs = generateSupportedAudioPTs(input)
										.getSupportedPTsForContentType(contentType);
			return order(setPTs);
		}
		return null;
	}
	
	private List order(Set setPTs) {
		List result = new ArrayList(setPTs); 
		Collections.sort(result, ptComparator);
		return result;
	}
	
	private EncodingSupportStore generateSupportedAudioPTs(InputStream input) {
		EncodingSupportStore result = new EncodingSupportStore();
		addSupportedAudioPTs(true, input, result);
		addSupportedAudioPTs(false, input, result);
		return result;
	}
	
	private void addSupportedAudioPTs(boolean encoding,
												InputStream input, EncodingSupportStore store) {
		
		Class[] mediaEngineArray;
		
		if(encoding) mediaEngineArray = encodingMediaEngines;
		else mediaEngineArray = decodingMediaEngines;
		
		for(int i = 0; i < mediaEngineArray.length; i++) {
//			use reflection to instantiate a member of the class
			try {

				Object[] args = {new PayloadType(PayloadType.INVALID_PT, "invalid"), this};
				MediaEngine me = (MediaEngine) getInstance(mediaEngineArray[i], args);
				
				if(encoding) me.setInput(input);
				Set supported = me.getSupportedPayloadTypes(ContentType.AUDIO);
				
				Iterator it = supported.iterator();
				
				while (it.hasNext()) {
					if(encoding) store.setEncodingSupport(ContentType.AUDIO, 
							(PayloadType) it.next(), mediaEngineArray[i]);
					else store.setDecodingSupport(ContentType.AUDIO, 
							(PayloadType) it.next(), mediaEngineArray[i]);
				}
					
			} catch (Exception e) {
//				Instantiation went wrong in some way. This can safely be ignored as the support
//				elements will not have been added to the array.
				e.printStackTrace();
			}
		}
	}
	
	private void initializeSupportedTransports() {
		supportedTransports = new TransportSupportStore();
		
		for(int i = 0; i < mediaTransports.length; i++) {
			try {
				Object[] args = {new TransportCandidate.Fixed()};
				MediaTransport mt = (MediaTransport) getInstance(mediaTransports[i], args);
				
				Set supported = mt.getSupportedProtocols();
				
				Iterator it = supported.iterator();
				
				while(it.hasNext()) supportedTransports.addElement(new TransportSupportElement(
						(Protocol)it.next(), mediaTransports[i]));
				
				
			} catch(Exception e) {
//				Instantiation went wrong in some way. This can safely be ignored as the support
//				elements will not have been added to the array.
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Get a MediaTransport object that implements the given transport type. Returns null if none
	 * is availbale.
	 * @param remoteCandidate The remote candidate to use for connecting to the remote service.
	 * @param localCandidate The local candidate where we must listen for connections.
	 * @return MediaTransport object implementing transport of the given type.
	 */
	public MediaTransport getMediaTransport(TransportCandidate remoteCandidate, 
			TransportCandidate localCandidate) {

		Class mtClass = supportedTransports.getClassForProtocol(MediaTransport.getProtocol(remoteCandidate));
		
		if(mtClass != null) {
			try {
				
				Object[] args = {remoteCandidate, localCandidate};
				return (MediaTransport) getInstance(mtClass, args);
				
			} catch (Exception e) {
				// instantiating the class did not work, so remove its pairing with this 
				// protocol from the list
				supportedTransports.remove(MediaTransport.getProtocol(remoteCandidate), mtClass);
			} 
		}
		return null;
	
	}
	
	private Object getInstance(Class c, Object[] args) throws SecurityException,
			NoSuchMethodException, IllegalArgumentException, InstantiationException,
				IllegalAccessException, InvocationTargetException {
		
		Class[] argTypes = new Class[args.length];
		for(int i = 0; i < args.length; i++) argTypes[i] = args[i].getClass();
		Constructor con = c.getConstructor(argTypes);
		
		return con.newInstance(args);
		
	}
	
	/**
	 * Add a new session request listener. This listener will be notified when new session requests
	 * are received.
	 * @param listener the listener to be added.
	 */
	public void addSessionRequestListener(JingleListener.SessionRequest listener) {
		jingleManager.addJingleSessionRequestListener(listener);
	}
	
	/**
	 * Remove a session request listener. This listener will no longer be notified of new session
	 * requests.
	 * @param listener the listener to be added.
	 */
	public void removeSessionRequestListener(JingleListener.SessionRequest listener) {
		jingleManager.removeJingleSessionRequestListener(listener);
	}
	
	/**
	 * @param comp Comparator to be used when sorting PayloadTypes into order of preference.
	 */
	public void setPTComparator(Comparator comp) {
		ptComparator = comp;
	}
	
	private Class[] getClassArray(Iterator it) {
		Set classes = new HashSet();
		while(it.hasNext()) classes.add(it.next().getClass());
		
		Class[] result = new Class[classes.size()];
		Iterator itClasses = classes.iterator();
		for(int i = 0; i < result.length; i++) result[i] = (Class) itClasses.next();
		return result;
	}
	
//	Inner classes -------------------------------------------------------------------
	
	private class EncodingSupportStore {
		
		private Set elements = new HashSet();
		
		private EncodingSupportElement getElementForPT(PayloadType pt) {
			Iterator it = elements.iterator();
			while(it.hasNext()) {
				EncodingSupportElement current = (EncodingSupportElement) it.next();
				if(current.getPayloadType().equals(pt)) return current;
			}
			return null;
		}
		
		public void setEncodingSupport(ContentType ct, PayloadType pt, Class encClass) {
			EncodingSupportElement toChange = getElementForPT(pt);
			if(toChange == null) {
				toChange = new EncodingSupportElement(ct, pt);
				toChange.setEncodingClass(encClass);
				elements.add(toChange);
			}
			else toChange.setEncodingClass(encClass);
		}
		
		public void setDecodingSupport(ContentType ct, PayloadType pt, Class decClass) {
			EncodingSupportElement toChange = getElementForPT(pt);
			if(toChange == null) {
				toChange = new EncodingSupportElement(ct, pt);
				toChange.setDecodingClass(decClass);
				elements.add(toChange);
			}
			else toChange.setDecodingClass(decClass);
		}
		
		public void remove(PayloadType pt, Class meClass) {
			
			EncodingSupportElement toRemove = getElementForPT(pt);
			if(toRemove.getEncodingClass().equals(meClass)) toRemove.setEncodingClass(null);
			if(toRemove.getDecodingClass().equals(meClass)) toRemove.setDecodingClass(null);
			if(toRemove.getDecodingClass() == null && toRemove.getEncodingClass() == null)
				elements.remove(toRemove);
		}

		public Set getSupportedPTsForContentType(ContentType contentType) {
			Set result = new HashSet();
			
			Iterator it = elements.iterator();
			
			while (it.hasNext()) {
				EncodingSupportElement current = (EncodingSupportElement) it.next();
				//only reurn PTs that we can encode and decode
				if(current.getContentType().equals(contentType)
						&& current.getEncodingClass() != null
						&& current.getDecodingClass() != null)
					result.add(current.getPayloadType());
			}
			
			return result;
		}
		
		public Class getEncodingClassForPT(PayloadType pt) {
			EncodingSupportElement toReturn = getElementForPT(pt);
			if(toReturn == null) return null;
			return toReturn.getEncodingClass();
		}
		
		public Class getDecodingClassForPT(PayloadType pt) {
			EncodingSupportElement toReturn = getElementForPT(pt);
			if(toReturn == null) return null;
			return toReturn.getEncodingClass();
		}
		
	}
	
	private class EncodingSupportElement {
		
		private ContentType contentType;
		private PayloadType payloadType;
		private Class encodingClass, decodingClass;
		
		public EncodingSupportElement(ContentType contentType, PayloadType payloadType) {
			this.contentType = contentType;
			this.payloadType = payloadType;
		}

		public Class getDecodingClass() {
			return decodingClass;
		}
		
		public void setDecodingClass(Class decodingClass) {
			this.decodingClass = decodingClass;
		}
		
		public Class getEncodingClass() {
			return encodingClass;
		}

		public void setEncodingClass(Class encodingClass) {
			this.encodingClass = encodingClass;
		}

		public ContentType getContentType() {
			return contentType;
		}

		public PayloadType getPayloadType() {
			return payloadType;
		}		
	}
	
	private class TransportSupportStore {
		private Set elements;
		
		public void addElement(TransportSupportElement element) {
			elements.add(element);
		}
		
		public void remove(Protocol proto, Class mtClass) {
			Iterator it = elements.iterator();
			while(it.hasNext()) {
				TransportSupportElement current = (TransportSupportElement) it.next();
				if(current.getProtocol().equals(proto) && current.getImplementingClass().equals(mtClass))
					elements.remove(current);
			}
		}

		public Class getClassForProtocol(TransportCandidate.Protocol protocol) {
			Iterator it = elements.iterator();
			while(it.hasNext()) {
				TransportSupportElement current = (TransportSupportElement) it.next();
				if(current.getProtocol().equals(protocol)) return current.getImplementingClass();
			}
			return null;
		}
	}
	
	private class TransportSupportElement {
		private TransportCandidate.Protocol protocol;
		private Class implementingClass;
		
		public TransportSupportElement(Protocol protocol, Class implementingClass) {
			this.protocol = protocol;
			this.implementingClass = implementingClass;
		}

		public Class getImplementingClass() {
			return implementingClass;
		}

		public TransportCandidate.Protocol getProtocol() {
			return protocol;
		}

	}
	
	public static class ContentType {
		
//		Media type constants. Used to differentiate between the different types of connections
//		that can be negotiated using Jingle.

		public static final ContentType AUDIO = new ContentType();
		public static final ContentType VIDEO = new ContentType();
		public static final ContentType AUDIO_AND_VIDEO = new ContentType();
		public static final ContentType FILE_SHARING = new ContentType();
		
		public boolean equals(Object o) {
			return o == this;
		}
	}	
	
	public static class SessionCreationError extends Error{ 
		private static final long serialVersionUID = 1L;
		public SessionCreationError(String text) {
			super(text);
		}
	}
	
	//Payload Type comparators------------------------------------------------------------------
	
	/**
	 * PayloadType comparator that orders PayloadTypes by preference level.
	 */
	public static class DefaultPTC implements Comparator {
		
		public int compare(Object arg0, Object arg1) {
			PayloadType pt0 = (PayloadType) arg0;
			PayloadType pt1 = (PayloadType) arg1;
			if(FormatTranslator.preferenceLevel(pt0) < FormatTranslator.preferenceLevel(pt1)) return 1;
			if(FormatTranslator.preferenceLevel(pt0) > FormatTranslator.preferenceLevel(pt1)) return -1;
			else return 0;
		}
	}
}

