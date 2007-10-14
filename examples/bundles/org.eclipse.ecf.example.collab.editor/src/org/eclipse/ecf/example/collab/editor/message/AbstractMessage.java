/*******************************************************************************
 * Copyright (c) 2006 Ken Gilmer. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Ken Gilmer - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.example.collab.editor.message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.eclipse.ecf.core.util.ECFException;

public abstract class AbstractMessage implements Serializable {

	private static final long serialVersionUID = 6948157547330289296L;

	public byte[] toByteArray() throws IOException, ECFException {
		final ByteArrayOutputStream bouts = new ByteArrayOutputStream();
		final ObjectOutputStream douts = new ObjectOutputStream(bouts);
		douts.writeObject(this);
		return bouts.toByteArray();
	}
}
