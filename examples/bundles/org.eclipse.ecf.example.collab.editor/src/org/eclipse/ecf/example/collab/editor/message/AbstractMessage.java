package org.eclipse.ecf.example.collab.editor.message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.eclipse.ecf.core.util.ECFException;

public abstract class AbstractMessage implements Serializable {
	
	
	public byte[] toByteArray() throws IOException, ECFException {
		ByteArrayOutputStream bouts = new ByteArrayOutputStream();
		ObjectOutputStream douts = new ObjectOutputStream(bouts);
		douts.writeObject(this);
		return bouts.toByteArray();
	}
}
