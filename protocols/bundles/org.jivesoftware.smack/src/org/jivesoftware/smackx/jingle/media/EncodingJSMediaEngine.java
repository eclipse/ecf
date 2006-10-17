package org.jivesoftware.smackx.jingle.media;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.jivesoftware.smackx.jingle.PayloadType;
import org.jivesoftware.smackx.jingle.media.JingleMediaManager.ContentType;
import org.jivesoftware.smackx.jingle.media.util.FormatTranslator;

public class EncodingJSMediaEngine extends MediaEngine.Encoding {
	
	private AudioInputStream input, output;
	private AudioFormat format;
	private int state;
	
	/**
	 * Zero argument constructor demanded by the SPI. Should not normally be used.
	 */
	public EncodingJSMediaEngine() {
		super(null);
	}

	public EncodingJSMediaEngine(PayloadType outputFormat) {
		super(outputFormat);
		format = FormatTranslator.toJSAudioFormat(outputFormat);
		state = INSTANTIATED;
	}

	public synchronized InputStream getInput() {
		return input;
	}

	public synchronized void setInput(InputStream input) {
		try {
			
			AudioInputStream aIS;
			if(input instanceof AudioInputStream) aIS = (AudioInputStream) input;
			else aIS = AudioSystem.getAudioInputStream(input);
			
			if((aIS.getFormat().getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED))
					|| (aIS.getFormat().getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED)))
						this.input = aIS;
			
			else throw new IllegalArgumentException("Input to EncodingJSMediaEngine should be PCM.");
			
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public synchronized InputStream getOutput() {
		return output;
	}

	public synchronized Set getSupportedPayloadTypes(ContentType contentType) {
		if(input == null) throw new InputNotSetError("Input must be set before calling getSupportedPayloadTypes.");
		Set result = new HashSet();
		
		if(contentType.equals(ContentType.AUDIO)) {
			AudioFormat.Encoding[] encodings = AudioSystem.getTargetEncodings(input.getFormat());
			
			for(int i = 0; i < encodings.length; i++) {
				AudioFormat[] formats = AudioSystem.getTargetFormats(encodings[i], input.getFormat());
				for(int j = 0; j < formats.length; j++)
					if(FormatTranslator.toPayloadType(formats[j]) != null)
							result.add(FormatTranslator.toPayloadType(formats[j]));
			}		
		}
		return result;
	}

	public synchronized int getState() {
		return state;
	}

	public synchronized void configure() throws UnableToConfigureMediaEngine {
		if(input == null) throw new InputNotSetError("Must set input before configuring MediaEngine.");
		if(state != INSTANTIATED) throw new StateOrderViolationError("MediaEngine should be in the instantiated state when configure is called.");
		output = AudioSystem.getAudioInputStream(format, input);
		state = CONFIGURED;
	}

	public synchronized void start() {
		if(state == CONFIGURED) state = STARTED;
		else throw new StateOrderViolationError("Tried to start MediaEngine without configuring first.");
	}

	public synchronized void stop() {
		if(state == STARTED) state = CONFIGURED;
		else throw new StateOrderViolationError("Tried to stop MediaEngine when it hasn't been started."); 
	}

	public synchronized void close() {
		try {
			if(output != null) {
				output.close();
				output = null;
			}
			state = CLOSED;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

