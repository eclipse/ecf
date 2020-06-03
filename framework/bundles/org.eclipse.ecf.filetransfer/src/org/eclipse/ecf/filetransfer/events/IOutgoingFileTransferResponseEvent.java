/****************************************************************************
 * Copyright (c) 2004 Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *               Cloudsmith, Inc. - additional API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.filetransfer.events;

import org.eclipse.ecf.filetransfer.*;

/**
 * Asynchronous event sent to {@link IFileTransferListener} associated with
 * {@link IOutgoingFileTransfer} instances when a response is received from the
 * remote target (or provider times out).
 * 
 */
public interface IOutgoingFileTransferResponseEvent extends IOutgoingFileTransferEvent {
	/**
	 * If request was accepted from remote target this method will return true,
	 * if rejected or failed returns false.
	 * 
	 * @return true if request was accepted, false if rejected or failed
	 */
	public boolean requestAccepted();

	/**
	 * Set the {@link FileTransferJob} to use for the actual file transfer.  This method only
	 * has effect if the {@link #requestAccepted()} returns <code>true</code>.
	 * @param job the job to use.  If <code>null</code>, or this method is not called, then
	 * a default FileTransferJob is used.  NOTE: the given job should
	 * *not* be scheduled/started prior to being provided as a parameter to this method.
	 */
	public void setFileTransferJob(FileTransferJob job);
}
