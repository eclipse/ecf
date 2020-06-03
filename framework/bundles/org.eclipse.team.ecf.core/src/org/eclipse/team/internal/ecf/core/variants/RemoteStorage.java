/****************************************************************************
 * Copyright (c) 2008 Versant Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Remy Chi Jian Suen (Versant Corporation) - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.team.internal.ecf.core.variants;

import java.io.*;
import org.eclipse.core.resources.IEncodedStorage;
import org.eclipse.core.runtime.*;

final class RemoteStorage implements IEncodedStorage, Serializable {

	private static final long serialVersionUID = -4773139009062649259L;

	private final String path;
	private final String charset;
	private final byte[] bytes;

	RemoteStorage(String path, String charset, byte[] bytes) {
		this.path = path;
		this.charset = charset;
		this.bytes = bytes;
	}

	public String getCharset() {
		return charset;
	}

	public InputStream getContents() {
		return new ByteArrayInputStream(bytes);
	}

	public IPath getFullPath() {
		return new Path(path);
	}

	public String getName() {
		return new Path(path).lastSegment();
	}

	public boolean isReadOnly() {
		return true;
	}

	public Object getAdapter(Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	public String toString() {
		return "RemoteStorage[path=" + path + ']'; //$NON-NLS-1$
	}

}
