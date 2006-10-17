package org.jivesoftware.smackx.jingle.media.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.media.format.AudioFormat;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class StreamConverter {
	
	private static Map ais2ds = new HashMap();
	private static Map ds2ais = new HashMap();

	public static DataSource toDataSource(AudioInputStream ais) {
		DataSource result = (DataSource) ais2ds.get(ais);
		if(result == null) {
			result = new AISDataSource(ais);
			try {
				result.connect();
				result.start();
			} catch (IOException e) {}
			ais2ds.put(ais, result);
		}
		return result;
	}
	
	public static DataSource toDataSource(InputStream is) {
		if(is instanceof AudioInputStream)
			return toDataSource((AudioInputStream) is);
		return null;
	}
	
	public static AudioInputStream toAudioInputStream(DataSource ds) {
		AudioInputStream result = (AudioInputStream) ds2ais.get(ds);
		if(result == null) {
			try {
				AudioFormat af = (AudioFormat) ((PushBufferDataSource) ds).getStreams()[0].getFormat();
				result = new AudioInputStream(toInputStream(ds), FormatTranslator.toJSAudioFormat(af), AudioSystem.NOT_SPECIFIED);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			ais2ds.put(ds, result);
		}
		return result;
	}
	
	public static InputStream toInputStream(DataSource ds) {
		if (ds instanceof PushBufferDataSource) {
			return new DSInputStream((PushBufferDataSource) ds, 40*1024);
		} else throw new Error("DataSource must be of class PushBufferDataSource");
	}

}

