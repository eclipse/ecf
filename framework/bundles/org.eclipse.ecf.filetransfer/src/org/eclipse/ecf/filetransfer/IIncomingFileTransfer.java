/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.filetransfer;

import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveStartEvent;

/**
 * Incoming file transfer request. Instance implementing this interface are
 * provided via calling the
 * {@link IIncomingFileTransferReceiveStartEvent#receive(java.io.File)} method.
 * 
 */
public interface IIncomingFileTransfer extends IFileTransfer {
	/**
	 * Get number of bytes received so far. If provider does not support
	 * reporting the number of bytes received, will return -1.
	 * 
	 * @return long number of bytes received. Returns -1 if provider does not
	 *         support reporting of number of bytes received during transfer
	 */
	public long getBytesReceived();
}
