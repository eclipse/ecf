package org.jivesoftware.smackx.jingle.media.util;

import java.io.IOException;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PushBufferStream;
import javax.sound.sampled.AudioInputStream;


public class AISPushBufferStream implements PushBufferStream, Runnable {
	
	private Format format;
	private AudioInputStream data;
	private ContentDescriptor cd = new ContentDescriptor(ContentDescriptor.RAW);
	
	private BufferTransferHandler transferHandler;
	private int seqNo;
	
	private Thread thread;
	private boolean threadStarted;
	
	public AISPushBufferStream(AudioInputStream ais) {
		data = ais;
		format = FormatTranslator.toJMFFormat(ais.getFormat());
		thread = new Thread(this);
		seqNo = 0;
	}

	public Format getFormat() {
		return format;
	}

	public synchronized void read(Buffer buff) throws IOException {
		
		
		int dataAvailable = 0;
		try {
			dataAvailable = data.available();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(dataAvailable > 0) {
			byte[] outdata = new byte[dataAvailable];
			int dataRead = data.read(outdata);
			
			buff.setFormat(getFormat());
			buff.setData(outdata);
			
			buff.setLength(dataRead);
			buff.setSequenceNumber(seqNo);
			buff.setHeader(null);
			buff.setFlags(0);
		}
		
		
	}

	public synchronized void setTransferHandler(BufferTransferHandler transferHandler) {
		this.transferHandler = transferHandler;
		notifyAll();
	}

	public ContentDescriptor getContentDescriptor() {
		return cd;
	}

	public long getContentLength() {
		return LENGTH_UNKNOWN;
	}

	public boolean endOfStream() {
		return false;
	}

	public Object[] getControls() {
		return null;
	}

	public Object getControl(String arg0) {
		//no controls
		return null;
	}
	
	public synchronized void start(boolean started) {
		threadStarted = started;
		if(started && !thread.isAlive()) {
			thread = new Thread(this);
			thread.start();
		}
		notifyAll();
	}

	public void run() {
		while(threadStarted) {
			synchronized(this) {
				while(transferHandler == null && threadStarted) {
					try {
						wait(1000);
					} catch(InterruptedException e) {}
				}
			}
			
			int dataAvailable = 0;
			try {
				dataAvailable = data.available();
			} catch (IOException e) {
				e.printStackTrace();
			}

			//loop until there is some data to be read
			if(threadStarted && transferHandler != null) {
				if(dataAvailable > 0) {
					transferHandler.transferData(this);
				}
				else try {
					Thread.sleep(10);
				} catch (InterruptedException e) {}
			}
		}
	}
	
}

