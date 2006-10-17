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

public class DecodingJSMediaEngine extends MediaEngine.Decoding {
	
	private static AudioFormat[] defaultOutputs = {new AudioFormat(8000, 8, 1, true, false),
		new AudioFormat(16000, 8, 1, true, false),
		new AudioFormat(44100, 8, 1, true, false),
		new AudioFormat(8000, 8, 2, true, false),
		new AudioFormat(16000, 8, 2, true, false),
		new AudioFormat(44100, 8, 2, true, false),
		new AudioFormat(8000, 16, 1, true, false),
		new AudioFormat(16000, 16, 1, true, false),
		new AudioFormat(44100, 16, 1, true, false),
		new AudioFormat(8000, 16, 2, true, false),
		new AudioFormat(16000, 16, 2, true, false),
		new AudioFormat(44100, 16, 2, true, false)};
	
	private AudioInputStream input, output;
	private AudioFormat format;
	private int state;
	
	
	/**
	 * Zero argument constructor demanded by the SPI. Should not normally be used.
	 */
	public DecodingJSMediaEngine() {
		super(null);
	}

	public DecodingJSMediaEngine(PayloadType inputFormat) {
		super(inputFormat);
		format = FormatTranslator.toJSAudioFormat(inputFormat);
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
			
			if(!FormatTranslator.equals(aIS.getFormat(),format)) throw new IllegalArgumentException("Incoming " +
					"audio stream does not have the correct format. Expected "+ format +" and got " +
							aIS.getFormat());
			this.input = aIS;
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		
	}

	public synchronized InputStream getOutput() {
		return output;
	}

	public synchronized Set getSupportedPayloadTypes(ContentType contentType) {
		Set result = new HashSet();
		
		if(contentType.equals(ContentType.AUDIO)) {
			
			for(int i = 0; i < defaultOutputs.length; i++) {
				AudioFormat outputFormat = defaultOutputs[i];
				
				AudioFormat.Encoding[] encodings = AudioSystem.getTargetEncodings(outputFormat);
				
				for(int j = 0; j < encodings.length; j++) {
					
					AudioFormat[] formats = AudioSystem.getTargetFormats(encodings[j], outputFormat);
					for(int k = 0; k < formats.length; k++) {
												
						if(AudioSystem.isConversionSupported(AudioFormat.Encoding.PCM_SIGNED, formats[k]) && FormatTranslator.toPayloadType(formats[k]) != null)
							result.add(FormatTranslator.toPayloadType(formats[k]));
					}
				}
			}
			
		}
		
		return result;
	}

	public synchronized int getState() {
		return state;
	}

	public synchronized void configure() throws UnableToConfigureMediaEngine {
		if(state != INSTANTIATED) throw new StateOrderViolationError("MediaEngine should be in the instantiated state when configure is called.");
		if(input == null) throw new InputNotSetError("Must set input before configuring MediaEngine.");
		output = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, input);
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

