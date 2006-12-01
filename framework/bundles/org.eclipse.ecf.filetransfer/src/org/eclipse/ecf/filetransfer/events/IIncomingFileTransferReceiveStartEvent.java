/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.filetransfer.events;

import java.io.*;

import org.eclipse.ecf.filetransfer.IFileTransferListener;
import org.eclipse.ecf.filetransfer.IIncomingFileTransfer;
import org.eclipse.ecf.filetransfer.identity.IFileID;

/**
 * Event sent to {@link IFileTransferListener} associated with
 * {@link IIncomingFileTransfer} instances
 * 
 */
public interface IIncomingFileTransferReceiveStartEvent extends
		IFileTransferEvent {

	/**
	 * Get IFileID for incoming file
	 * 
	 * @return IFileID for this file transfer event.  Will not be null.
	 */
	public IFileID getFileID();

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

	/**
	 * Get incoming file transfer.
	 * 
	 * @param streamToStore
	 *            the output stream to store the incoming file
	 * @return IIncomingFileTransfer the incoming file transfer object
	 * @throws IOException
	 *             if streamToStore cannot be opened for writing
	 */
	public IIncomingFileTransfer receive(OutputStream streamToStore) throws IOException;
	
	/**
	 * Cancel incoming file transfer
	 */
	public void cancel();
}
