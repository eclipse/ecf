/****************************************************************************
 * Copyright (c) 2004 Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.filetransfer;

/**
 * Outgoing file transfer.
 * 
 */
public interface IOutgoingFileTransfer extends IFileTransfer {
	/**
	 * Get the number of bytes sent for this outgoing file transfer. Returns 0
	 * if transfer has not be started, and -1 if underlying provider does not
	 * support reporting number of bytes sent during transfer.
	 * 
	 * @return number of bytes sent. Returns 0 if the outgoing file transfer has
	 *         not been started, and -1 if provider does not support reporting
	 *         of number of bytes received during transfer
	 */
	public long getBytesSent();
}
