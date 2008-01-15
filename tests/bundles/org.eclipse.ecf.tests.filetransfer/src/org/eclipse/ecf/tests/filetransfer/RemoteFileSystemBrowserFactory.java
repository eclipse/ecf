package org.eclipse.ecf.tests.filetransfer;

import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.filetransfer.IRemoteFileSystemListener;
import org.eclipse.ecf.filetransfer.IRemoteFileSystemRequest;
import org.eclipse.ecf.filetransfer.RemoteFileSystemException;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.filetransfer.service.IRemoteFileSystemBrowser;
import org.eclipse.ecf.filetransfer.service.IRemoteFileSystemBrowserFactory;
import org.eclipse.ecf.provider.filetransfer.browse.LocalFileSystemBrowser;
import org.eclipse.ecf.provider.filetransfer.identity.FileTransferNamespace;

public class RemoteFileSystemBrowserFactory implements IRemoteFileSystemBrowserFactory {

	public RemoteFileSystemBrowserFactory() {
		// nothing
	}

	public IRemoteFileSystemBrowser newInstance() {
		return new IRemoteFileSystemBrowser() {

			public Namespace getBrowseNamespace() {
				return IDFactory.getDefault().getNamespaceByName(FileTransferNamespace.PROTOCOL);
			}

			public IRemoteFileSystemRequest sendBrowseRequest(IFileID directoryID, IRemoteFileSystemListener listener) throws RemoteFileSystemException {
				final LocalFileSystemBrowser fsb = new LocalFileSystemBrowser(directoryID, listener);
				return fsb.sendBrowseRequest();
			}

			public Object getAdapter(Class adapter) {
				return null;
			}
		};
	}

}
