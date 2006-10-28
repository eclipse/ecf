/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.filetransfer.events;

import org.eclipse.ecf.filetransfer.IFileTransferListener;
import org.eclipse.ecf.filetransfer.IOutgoingFileTransfer;

/**
 * Asynchronous event sent to {@link IFileTransferListener} associated with
 * {@link IOutgoingFileTransfer} instances when a response is received from the
 * remote target (or provider times out).
 * 
 */
public interface IOutgoingFileTransferResponseEvent extends
		IOutgoingFileTransferEvent {
	/**
	 * If request was accepted from remote target this method will return true,
	 * if rejected or failed returns false.
	 * 
	 * @return true if request was accepted, false if rejected or failed
	 */
	public boolean requestAccepted();

}
