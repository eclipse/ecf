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

package org.eclipse.ecf.filetransfer;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.filetransfer.identity.IFileID;

/**
 * Remote file system browser adapter.  This adapter can be retrieved from a container
 * for exposing remote file system browsing capabilities.
 */
public interface IRemoteFileSystemBrowserContainerAdapter extends IAdaptable {

	/**
	 * Get the {@link Namespace} instance for creating IFileIDs that represent remote directories.
	 * 
	 * @return Namespace for directories.  Will not be <code>null</code>.
	 */
	public Namespace getDirectoryNamespace();

	/**
	 * Send a request for directory information for given directoryID.
	 * @param directoryID the IFileID representing/specifying the remote directory to access.
	 * @param listener the listener that will be notified asynchronously when a response to this request is received.  Must not be
	 * <code>null</code>.  
	 * @return IRemoteFileSystemRequest the request instance.
	 */
	public IRemoteFileSystemRequest sendDirectoryRequest(IFileID directoryID, IRemoteFileSystemListener listener) throws RemoteFileSystemException;

}
