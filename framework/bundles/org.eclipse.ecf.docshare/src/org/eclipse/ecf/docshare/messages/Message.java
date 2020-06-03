/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.docshare.messages;

import java.io.*;
import org.eclipse.ecf.internal.docshare.Messages;
import org.eclipse.ecf.sync.IModelChangeMessage;
import org.eclipse.ecf.sync.SerializationException;

/**
 * @since 2.1
 *
 */
public class Message implements IModelChangeMessage, Serializable {

	private static final long serialVersionUID = 4858801311305630711L;

	/**
	 * Deserialize in to message
	 * @param bytes
	 * @return IModelChangeMessage
	 * @throws SerializationException
	 */
	public static IModelChangeMessage deserialize(byte[] bytes) throws SerializationException {
		try {
			final ByteArrayInputStream bins = new ByteArrayInputStream(bytes);
			final ObjectInputStream oins = new ObjectInputStream(bins);
			return (IModelChangeMessage) oins.readObject();
		} catch (final Exception e) {
			throw new SerializationException(Messages.DocShare_EXCEPTION_DESERIALIZING_MESSAGE0, e);
		}
	}

	public byte[] serialize() throws SerializationException {
		try {
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			final ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(this);
			return bos.toByteArray();
		} catch (final Exception e) {
			throw new SerializationException(Messages.DocShare_EXCEPTION_DESERIALIZING_MESSAGE0, e);
		}
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

}
