/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.filetransfer.events;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.eclipse.ecf.filetransfer.IFileTransferListener;
import org.eclipse.ecf.filetransfer.IIncomingFileTransfer;
import org.eclipse.ecf.filetransfer.IOutgoingFileTransfer;

/**
 * Event sent to {@link IFileTransferListener} associated with
 * {@link IOutgoingFileTransfer} instances
 * 
 */
public interface IIncomingFileTransferReceiveStartEvent extends
		IFileTransferEvent {
	
	/**
	 * Get URI of incoming file
	 * @return URI of incoming file
	 */
	public URI getURI();
	
	/**
	 * Get incoming file transfer.
	 * 
	 * @param localFileToSave
	 *            the file on the local file system to receive and save the
	 *            remote file
	 * @return IIncomingFileTransfer the incoming file transfer object
	 * @throws IOException
	 *             if localFileToSave cannot be opened for writing
	 */
	public IIncomingFileTransfer receive(File localFileToSave)
			throws IOException;
}
