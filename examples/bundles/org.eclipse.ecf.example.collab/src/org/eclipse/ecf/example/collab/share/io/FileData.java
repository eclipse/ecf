/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
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

package org.eclipse.ecf.example.collab.share.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class FileData implements Serializable {
	static final long serialVersionUID = 469244980886541978L;
	byte[] myData;
	int read;

	FileData(InputStream ins, int chunkSize) throws IOException {
		myData = new byte[chunkSize];
		read = ins.read(myData);
	}

	FileData() {
		read = -1;
	}

	void saveData(OutputStream aFileStream) throws IOException {
		if (read != -1)
			aFileStream.write(myData, 0, read);
	}

	boolean isDone() {
		return (read == -1);
	}

	int getDataSize() {
		return read;
	}

	public String toString() {
		return "FileData[" + read + ";" + isDone() + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}