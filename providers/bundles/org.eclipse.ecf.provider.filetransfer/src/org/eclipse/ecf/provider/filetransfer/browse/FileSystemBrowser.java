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
import java.net.MalformedURLException;
import org.eclipse.ecf.filetransfer.IRemoteFileSystemListener;
import org.eclipse.ecf.filetransfer.RemoteFileSystemException;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.internal.provider.filetransfer.Messages;
import org.eclipse.osgi.util.NLS;

/**
 *
 */
public class FileSystemBrowser extends AbstractFileSystemBrowser {

	protected File localDirectory;

	/**
	 * @param listener 
	 * @param directoryID2 
	 */
	public FileSystemBrowser(IFileID directoryID2, IRemoteFileSystemListener listener) throws RemoteFileSystemException {
		super(directoryID2, listener);
		try {
			localDirectory = new File(directoryID2.getURL().getPath());
		} catch (MalformedURLException e) {
			throw new RemoteFileSystemException(e);
		}
		if (!localDirectory.exists())
			throw new RemoteFileSystemException(NLS.bind(Messages.FileSystemBrowser_EXCEPTION_DIRECTORY_DOES_NOT_EXIST, localDirectory));
		if (!localDirectory.isDirectory())
			throw new RemoteFileSystemException(NLS.bind(Messages.FileSystemBrowser_EXCEPTION_NOT_DIRECTORY, localDirectory));
	}

	protected void runDirectoryRequest() throws Exception {
		File[] files = localDirectory.listFiles();
		remoteFiles = new LocalFile[files.length];
		for (int i = 0; i < files.length; i++) {
			remoteFiles[i] = new LocalFile(files[i]);
		}
	}

}
