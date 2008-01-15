/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.provider.filetransfer.browse;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import org.eclipse.ecf.filetransfer.IRemoteFile;
import org.eclipse.ecf.filetransfer.IRemoteFileSystemListener;
import org.eclipse.ecf.filetransfer.identity.IFileID;

/**
 *
 */
public class URLFileSystemBrowser extends AbstractFileSystemBrowser {

	URL directoryOrFile;

	/**
	 * @param directoryOrFileID
	 * @param listener
	 */
	public URLFileSystemBrowser(IFileID directoryOrFileID, IRemoteFileSystemListener listener, URL directoryOrFileURL) {
		super(directoryOrFileID, listener);
		this.directoryOrFile = directoryOrFileURL;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.filetransfer.browse.AbstractFileSystemBrowser#runRequest()
	 */
	protected void runRequest() throws Exception {
		URLConnection urlConnection = directoryOrFile.openConnection();
		urlConnection.connect();
		InputStream ins = urlConnection.getInputStream();
		ins.close();
		remoteFiles = new IRemoteFile[1];
		remoteFiles[0] = new URLRemoteFile(urlConnection, fileID);
	}

}
