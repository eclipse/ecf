/****************************************************************************
 * Copyright (c) 2007, 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *    Mustafa K. Isik
 *****************************************************************************/

package org.eclipse.ecf.sync.doc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.ecf.internal.sync.Activator;
import org.eclipse.ecf.sync.IModelChangeMessage;
import org.eclipse.ecf.sync.SerializationException;

/**
 * 
 */
public class DocumentChangeMessage implements IDocumentChange, IModelChangeMessage, Serializable {

	private static final long serialVersionUID = -3195542805471664496L;

	public static DocumentChangeMessage deserialize(byte[] bytes) throws SerializationException {
		try {
			final ByteArrayInputStream bins = new ByteArrayInputStream(bytes);
			final ObjectInputStream oins = new ObjectInputStream(bins);
			return (DocumentChangeMessage) oins.readObject();
		} catch (final Exception e) {
			throw new SerializationException("Exception deserializing DocumentChangeMessage", e);
		}
	}

	private String text;
	private int offset;
	private int length;

	public DocumentChangeMessage(int offset, int length, String text) {
		this.offset = offset;
		this.length = length;
		this.text = text;
	}

	/**
	 * Returns the modification index of the operation resembled by this
	 * message.
	 * 
	 * @return modification index
	 */
	public int getOffset() {
		return offset;
	}

	public void setOffset(int updatedOffset) {
		this.offset = updatedOffset;
	}

	/**
	 * Returns the length of replaced text.
	 * 
	 * @return length of replaced text
	 */
	public int getLengthOfReplacedText() {
		return length;
	}

	public void setLengthOfReplacedText(int length) {
		this.length = length;
	}

	public String getText() {
		return text;
	}

	public int getLengthOfInsertedText() {
		return this.text.length();
	}

	public String toString() {
		final StringBuffer buf = new StringBuffer("DocumentChangeMessage["); //$NON-NLS-1$
		buf.append("text=").append(text).append(";offset=").append(offset); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append(";length=").append(length).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		return buf.toString();
	}

	private byte[] serializeLocal() throws IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(this);
		return bos.toByteArray();
	}

	public byte[] serialize() throws SerializationException {
		try {
			return serializeLocal();
		} catch (final IOException e) {
			throw new SerializationException("Exception serializing DocumentChangeMessage", e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == null) return null;
		IAdapterManager manager = Activator.getDefault().getAdapterManager();
		if (manager == null) return null;
		return manager.loadAdapter(this, adapter.getName());
	}

}
