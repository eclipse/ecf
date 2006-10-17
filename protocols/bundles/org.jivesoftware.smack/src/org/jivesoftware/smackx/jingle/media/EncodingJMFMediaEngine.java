package org.jivesoftware.smackx.jingle.media;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.media.Controller;
import javax.media.Manager;
import javax.media.NoProcessorException;
import javax.media.Processor;
import javax.media.control.TrackControl;
import javax.media.format.AudioFormat;
import javax.media.protocol.DataSource;
import javax.sound.sampled.AudioInputStream;

import org.jivesoftware.smackx.jingle.PayloadType;
import org.jivesoftware.smackx.jingle.media.JingleMediaManager.ContentType;
import org.jivesoftware.smackx.jingle.media.util.FormatTranslator;
import org.jivesoftware.smackx.jingle.media.util.StreamConverter;

public class EncodingJMFMediaEngine extends MediaEngine.Encoding {
	
	protected static final AudioFormat[] supported = {
		 new AudioFormat(AudioFormat.LINEAR, 44100.0, 16, 2, AudioFormat.BIG_ENDIAN, AudioFormat.SIGNED, AudioFormat.NOT_SPECIFIED, AudioFormat.NOT_SPECIFIED, byte[].class),
		 new AudioFormat(AudioFormat.LINEAR, 44100.0, 16, 1, AudioFormat.BIG_ENDIAN, AudioFormat.SIGNED, AudioFormat.NOT_SPECIFIED, AudioFormat.NOT_SPECIFIED, byte[].class)
	}; 
	
	private Processor processor;
	private InputStream input;
	private AudioInputStream aisOutput; 
	
	public EncodingJMFMediaEngine() {
		super(null);
	}
	
	public EncodingJMFMediaEngine(PayloadType outputFormat) {
		super(outputFormat);
	}

	public Set getSupportedPayloadTypes(ContentType contentType) {
		if(contentType == ContentType.AUDIO) {
			Set result = new HashSet();
			
			for(int i = 0; i < supported.length; i++) {
				
				PayloadType pt = FormatTranslator.toPayloadType(supported[i]); 
				if(pt!=null) result.add(pt);
			}
			
			return result;
		}
		return null;
//		//first check the cache
//		Map cachedResultByDS = (Map) supportedPTs.get(StreamConverter.toDataSource(getInput()));
//		if(cachedResultByDS != null) {
//			Set cachedResultByContentType = (Set) cachedResultByDS.get(contentType);
//			if(cachedResultByContentType != null) return cachedResultByContentType;
//		}
//		
//		
//		// currently only supports audio
//		if(contentType.equals(ContentType.AUDIO)) {
//			if(getInput() == null) throw new MediaEngine.InputNotSetError("Must set input before" +
//			" asking which formats it supports.");
//			
//			DataSource inputDS = StreamConverter.toDataSource(input);
//			
//			
//			//this is a little bit of a hack, but it beats hard coding it.
//			Processor p;
//			try {
//				p = Manager.createProcessor(inputDS);
//				p.configure();
//				
//				waitForState(Processor.Configured, p);
//				
//				TrackControl[] tracks = p.getTrackControls();
//				
//				ContentDescriptor cd = new ContentDescriptor(ContentDescriptor.CONTENT_UNKNOWN);
//				p.setContentDescriptor(cd);
//				
//				Format[] formats = tracks[0].getSupportedFormats();
//				Set result = new HashSet();
//				
//				for(int i = 0; i < formats.length; i++) {
//					PayloadType pt = FormatTranslator.toPayloadType(formats[i]);
//					if(pt!=null)System.out.println(formats[i] + "   -   " + pt.getName() + ", " + pt.getChannels() + ", " + pt.getId());
//					if(pt!=null) result.add(pt);
//				}
//				
//				//add the result to the cache
//				if(cachedResultByDS != null) cachedResultByDS.put(contentType, result);
//				else {
//					Map newCTMap = new HashMap();
//					newCTMap.put(contentType, result);
//					supportedPTs.put(inputDS, newCTMap);
//				}
//				
//				return result;
//				
//			} catch (Exception e) {
//				//not sure if much more can be done here.
//				e.printStackTrace();
//			}
//			
//		}
//		return null;
	}
	
	public void configure() throws UnableToConfigureMediaEngine {
		
		DataSource inputDS = StreamConverter.toDataSource(input);
				
		if(inputDS == null) throw new InputNotSetError("Input must be set before configuring.");
				
		try {
			processor = Manager.createProcessor(inputDS);
			
			processor.configure();
			waitForState(Processor.Configured, processor);
			
			TrackControl[] tracks = processor.getTrackControls();
			try {
				tracks[0].setFormat(FormatTranslator.toJMFFormat(getPayloadType()));
			} catch (Exception e) {
				throw new UnableToConfigureMediaEngine(e);
			}
			
			processor.realize();

		} catch (NoProcessorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public void start() {
		processor.start();
	}

	public void stop() {
		processor.stop();
		
	}

	public int getState() {
		if(processor == null) return INSTANTIATED;
		else if(processor.getState() == Processor.Configuring ||
				processor.getState() == Processor.Configured ||
				processor.getState() == Controller.Realizing) return CONFIGURING;
		else if(processor.getState() == Controller.Realized) return CONFIGURED;
		else if(processor.getState() == Controller.Started) return STARTED;
		else return INSTANTIATED;
	}

	public void close() {
		processor.close();
		processor = null;
		try {
			getOutput().close();
		} catch (IOException e) {}
	}
	
	/**
	 * Block until processor p enters the given state.
	 */
	private void waitForState(int state, Processor p){
		while(p.getState() != state) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {}
		}
	}

	public InputStream getOutput() {
		if(processor == null || (processor.getState() != Processor.Realized && processor.getState() != Processor.Started)) aisOutput = null;
		else if(aisOutput == null) aisOutput = StreamConverter.toAudioInputStream(processor.getDataOutput());
		
		return aisOutput;
	}

	public InputStream getInput() {
		return input;
	}

	public void setInput(InputStream input) {
		if(processor == null) this.input = input;
	}

}

