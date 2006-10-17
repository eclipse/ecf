package org.jivesoftware.smackx.jingle.media.util;

import java.io.IOException;
import java.io.InputStream;

import javax.media.Buffer;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;

public class DSInputStream extends InputStream implements BufferTransferHandler {
	
	private byte[] circularByteBuffer;
	private int dataStart;
	private int dataEnd;
	
	private PushBufferDataSource input;
	
	public DSInputStream (PushBufferDataSource input, int bufferSize) {
		input.getStreams()[0].setTransferHandler(this);
		this.input = input;
		dataStart = 0;
		dataEnd = 0;
		circularByteBuffer = new byte[bufferSize];
	}

	public int read() throws IOException {
		while(true) {
			if(dataStart == dataEnd) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {}
			}
			else synchronized(circularByteBuffer) {
				int returnValue = circularByteBuffer[dataStart];
				if(dataStart == circularByteBuffer.length - 1) dataStart = 0;
				else dataStart ++;
				return returnValue;
			}
		}
	}
	
	public int available() {
		synchronized(circularByteBuffer) {
			if(dataStart <= dataEnd) return dataEnd - dataStart;
			else return dataEnd - dataStart + circularByteBuffer.length + 1;
		}
	}
	
	private void addData(byte[] data, int offset, int length) {
		if(data.length > 0 && offset + length <= data.length && offset >= 0 && length > 0) {
			synchronized(circularByteBuffer) {
				for(int i = offset; i < offset + length; i++) {
					if(dataEnd == circularByteBuffer.length - 1) {
//						wrap around
						circularByteBuffer[0] = data[i];
						dataEnd = 0;
					} else {
						circularByteBuffer[dataEnd + 1] = data[i];
						dataEnd++;
					}

//					if data has been overwritten then move on the start marker
					if(dataStart == dataEnd) dataStart++;
					if(dataStart == circularByteBuffer.length) dataStart = 0;
				}
			}
		}
	}

	public int read(byte[] b, int off, int len) {
		if(b == null) throw new NullPointerException();
		if(off < 0 || len < 0 || off + len > b.length) throw new IndexOutOfBoundsException();
		if(len > 0) {
			synchronized(circularByteBuffer) {
				int bytesRead = 0;
				
				for(int i = off; i < off+len; i++) {
					if(dataStart == dataEnd) break;
					b[i] = circularByteBuffer[dataStart];
					bytesRead++;
					if(dataStart == circularByteBuffer.length - 1) dataStart = 0;
					else dataStart ++;
				}
				return bytesRead;
			}
		}
		return 0;
	}
	
	public int read(byte[] b) {
		return read(b, 0, b.length);
	}
	
	public void close() {
		synchronized(circularByteBuffer) {
			circularByteBuffer = null;
			input.getStreams()[0].setTransferHandler(null);
		}
	}

	public void transferData(PushBufferStream pbs) {
		Buffer buff = new Buffer();
		try {
			pbs.read(buff);
			byte[] data = (byte[]) buff.getData();
			addData(data, buff.getOffset(), buff.getLength());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public boolean markSupported() {
		return false;
	}
}

