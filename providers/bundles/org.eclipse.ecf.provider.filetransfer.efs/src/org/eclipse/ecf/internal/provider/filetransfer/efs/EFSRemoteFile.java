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

package org.eclipse.ecf.internal.provider.filetransfer.efs;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.ecf.filetransfer.IRemoteFile;
import org.eclipse.ecf.filetransfer.IRemoteFileInfo;
import org.eclipse.ecf.filetransfer.identity.IFileID;

/**
 *
 */
public class EFSRemoteFile implements IRemoteFile {

	private final IFileID fileID;
	private final EFSRemoteFileInfo remoteFileInfo;

	/**
	 * @param fileID
	 * @param remoteFile
	 */
	public EFSRemoteFile(IFileID fileID, IFileInfo remoteFile) {
		this.fileID = fileID;
		this.remoteFileInfo = new EFSRemoteFileInfo(remoteFile);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.IRemoteFile#getID()
	 */
	public IFileID getID() {
		return fileID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.IRemoteFile#getInfo()
	 */
	public IRemoteFileInfo getInfo() {
		return remoteFileInfo;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

	public String toString() {
		final StringBuffer buf = new StringBuffer("EFSRemoteFile["); //$NON-NLS-1$
		buf.append("name=").append(getInfo().getName()).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		return buf.toString();
	}
}
