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
package org.eclipse.ecf.filetransfer.events;

import org.eclipse.ecf.filetransfer.IFileTransferListener;
import org.eclipse.ecf.filetransfer.IOutgoingFileTransfer;

/**
 * Event sent to {@link IFileTransferListener} associated with
 * {@link IOutgoingFileTransfer} instances when some data are received
 * 
 */
public interface IOutgoingFileTransferSendDataEvent extends IOutgoingFileTransferEvent {
	// no methods for interface
}
