package org.eclipse.ecf.internal.provider.filetransfer.efs;

import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransfer;
import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransferFactory;

public class EFSRetrieveFileTransferFactory implements
		IRetrieveFileTransferFactory {

	public IRetrieveFileTransfer newInstance() {
		return new EFSRetrieveFileTransfer();
	}

}
