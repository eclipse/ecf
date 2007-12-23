/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.provider.filetransfer.browse;

import java.io.File;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.filetransfer.*;
import org.eclipse.ecf.filetransfer.identity.FileIDFactory;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.provider.filetransfer.identity.FileTransferNamespace;

/**
 * Local representation of an {@link IRemoteFile}.
 */
public class LocalFile implements IRemoteFile {

	File file = null;

	IRemoteFileInfo info;

	/**
	 * @param file
	 */
	public LocalFile(File file) {
		this.file = file;
		Assert.isNotNull(file);
		this.info = new IRemoteFileInfo() {

			IRemoteFileAttributes attributes = new LocalFileAttributes(LocalFile.this.file);

			public IRemoteFileAttributes getAttributes() {
				return attributes;
			}

			public long getLastModified() {
				return LocalFile.this.file.lastModified();
			}

			public long getLength() {
				return LocalFile.this.file.length();
			}

			public String getName() {
				return LocalFile.this.file.getName();
			}

			public boolean isDirectory() {
				return LocalFile.this.file.isDirectory();
			}

			public void setAttributes(IRemoteFileAttributes attributes) {
				// can't set attributes
			}

			public void setLastModified(long time) {
				// can't set post hoc
			}

			public void setName(String name) {
				// Can't modify post hoc
			}
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.IRemoteFile#getID()
	 */
	public IFileID getID() {
		try {
			return FileIDFactory.getDefault().createFileID(IDFactory.getDefault().getNamespaceByName(FileTransferNamespace.PROTOCOL), file.toURL());
		} catch (Exception e) {
			// Should never happen
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.IRemoteFile#getInfo()
	 */
	public IRemoteFileInfo getInfo() {
		return info;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("LocalFile["); //$NON-NLS-1$
		sb.append("id=").append(getID()).append(";"); //$NON-NLS-1$//$NON-NLS-2$
		sb.append("name=").append(getInfo().getName()).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		return sb.toString();

	}
}
