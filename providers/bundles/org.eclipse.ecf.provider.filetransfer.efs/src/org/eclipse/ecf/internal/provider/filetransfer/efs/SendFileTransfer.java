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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.ecf.core.util.Proxy;
import org.eclipse.ecf.filetransfer.SendFileTransferException;
import org.eclipse.ecf.provider.filetransfer.outgoing.AbstractOutgoingFileTransfer;
import org.eclipse.ecf.provider.filetransfer.util.JREProxyHelper;

/**
 *
 */
public class SendFileTransfer extends AbstractOutgoingFileTransfer {

	JREProxyHelper proxyHelper = null;

	public SendFileTransfer() {
		super();
		this.proxyHelper = new JREProxyHelper();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.filetransfer.outgoing.AbstractOutgoingFileTransfer#hardClose()
	 */
	protected void hardClose() {
		super.hardClose();
		if (proxyHelper != null) {
			proxyHelper.dispose();
			proxyHelper = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.filetransfer.outgoing.AbstractOutgoingFileTransfer#openStreams()
	 */
	protected void openStreams() throws SendFileTransferException {
		try {
			// Get/open input file
			setInputStream(new BufferedInputStream(new FileInputStream(getFileTransferInfo().getFile())));
			// Open target
			final IFileStore fileStore = EFS.getStore(new URI(getRemoteFileURL().getPath()));
			setOutputStream(fileStore.openOutputStream(0, null));
		} catch (final Exception e) {
			throw new SendFileTransferException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.filetransfer.outgoing.AbstractOutgoingFileTransfer#setupProxy(org.eclipse.ecf.core.util.Proxy)
	 */
	protected void setupProxy(Proxy proxy) {
		proxyHelper.setupProxy(proxy);
	}

}
