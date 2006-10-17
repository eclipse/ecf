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

public class DecodingJMFMediaEngine extends MediaEngine.Decoding {
	
	private InputStream input;
	private AudioInputStream aisOutput;
	
	private Processor processor;
	
	public DecodingJMFMediaEngine() {
		super(null);
	}

	public DecodingJMFMediaEngine(PayloadType inputFormat) {
		super(inputFormat);
	}

	public Set getSupportedPayloadTypes(ContentType contentType) {
		if(contentType == ContentType.AUDIO) {
		Set result = new HashSet();
		
		for(int i = 0; i < EncodingJMFMediaEngine.supported.length; i++) {
			
			PayloadType pt = FormatTranslator.toPayloadType(EncodingJMFMediaEngine.supported[i]); 
			if(pt!=null) result.add(pt);
		}
		
		return result;
	}
	return null;
	}

	public void start() {
		processor.start();
		
	}

	public void stop() {
		processor.stop();
	}

	public void configure() {
		
		if(input == null) throw new InputNotSetError("Input must be set before configuring.");
		DataSource inputDS = StreamConverter.toDataSource(input);
		
		try {
			processor = Manager.createProcessor(inputDS);
			
			processor.configure();
			waitForState(Processor.Configured, processor);
			
			TrackControl[] tracks = processor.getTrackControls();
			
			tracks[0].setFormat(new AudioFormat(AudioFormat.LINEAR, 44100.0, 16, 2));
			
			processor.realize();
			
		} catch (NoProcessorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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

	public InputStream getOutput() {
		if(processor == null || (processor.getState() != Processor.Realized && processor.getState() != Processor.Started)) aisOutput = null;
		else if(aisOutput == null) aisOutput = StreamConverter.toAudioInputStream(processor.getDataOutput());
		
		return aisOutput;
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

	public InputStream getInput() {
		return input;
	}

	public void setInput(InputStream input) {
		if(processor == null) this.input = input;
	}

}

