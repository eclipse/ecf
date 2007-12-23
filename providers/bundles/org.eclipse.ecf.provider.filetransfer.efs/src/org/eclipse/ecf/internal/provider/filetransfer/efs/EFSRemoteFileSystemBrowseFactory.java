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

package org.eclipse.ecf.internal.provider.filetransfer.efs;

import java.net.URI;
import java.net.URL;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.filetransfer.IRemoteFileSystemListener;
import org.eclipse.ecf.filetransfer.IRemoteFileSystemRequest;
import org.eclipse.ecf.filetransfer.RemoteFileSystemException;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.filetransfer.service.IRemoteFileSystemBrowser;
import org.eclipse.ecf.filetransfer.service.IRemoteFileSystemBrowserFactory;
import org.eclipse.ecf.provider.filetransfer.identity.FileTransferNamespace;
import org.eclipse.osgi.util.NLS;

/**
 *
 */
public class EFSRemoteFileSystemBrowseFactory implements IRemoteFileSystemBrowserFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.service.IRemoteFileSystemBrowserFactory#newInstance()
	 */
	public IRemoteFileSystemBrowser newInstance() {
		return new IRemoteFileSystemBrowser() {

			public Namespace getDirectoryNamespace() {
				return IDFactory.getDefault().getNamespaceByName(FileTransferNamespace.PROTOCOL);
			}

			public IRemoteFileSystemRequest sendDirectoryRequest(IFileID directoryID, IRemoteFileSystemListener listener) throws RemoteFileSystemException {
				Assert.isNotNull(directoryID);
				Assert.isNotNull(listener);
				IFileStore directoryStore;
				URL efsDirectory = null;
				try {
					efsDirectory = directoryID.getURL();
					directoryStore = EFS.getStore(new URI(efsDirectory.getPath()));
				} catch (final Exception e) {
					throw new RemoteFileSystemException(e);
				}
				final IFileInfo directoryStoreInfo = directoryStore.fetchInfo();
				if (!directoryStoreInfo.isDirectory())
					throw new RemoteFileSystemException(NLS.bind(Messages.EFSRemoteFileSystemBrowseFactory_EXCEPTION_NOT_DIRECTORY, directoryID));
				final FileStoreBrowser rfs = new FileStoreBrowser(directoryStore, efsDirectory, directoryID, listener);
				return rfs.sendDirectoryRequest();

			}

			public Object getAdapter(Class adapter) {
				return null;
			}
		};
	}

}
