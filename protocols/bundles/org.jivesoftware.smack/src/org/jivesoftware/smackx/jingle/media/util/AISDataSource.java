package org.jivesoftware.smackx.jingle.media.util;

import java.io.IOException;

import javax.media.Time;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.sound.sampled.AudioInputStream;

public class AISDataSource extends PushBufferDataSource {
	
	private AISPushBufferStream[] streams = new AISPushBufferStream[1];
	private boolean connected = false;
	private boolean started = false; 
	
	public AISDataSource(AudioInputStream ais) {
		streams[0] = new AISPushBufferStream(ais);
	}

	public PushBufferStream[] getStreams() {
		return streams;
	}

	public String getContentType() {
		if(!connected) {
			System.err.println("Error: DataSource not connected");
			return null;
		}
		return "raw";
	}

	public void connect() throws IOException {
		connected = true;
	}

	public void disconnect() {
		try {
			if(started) stop();
		} catch (IOException e) {}
		connected = false;
	}

	public void start() throws IOException {
		if(!connected) throw new Error("DataSource must be connected before it can be started");
		if(!started) {
			started = true;
			streams[0].start(true);
		}
		
	}

	public void stop() throws IOException {
		if(connected && started) {
			started = false;
			streams[0].start(false);
		}
	}

	public Object getControl(String arg0) {
		return null;
	}

	public Object[] getControls() {
		return null;
	}

	public Time getDuration() {
		return DURATION_UNKNOWN;
	}
	
}
