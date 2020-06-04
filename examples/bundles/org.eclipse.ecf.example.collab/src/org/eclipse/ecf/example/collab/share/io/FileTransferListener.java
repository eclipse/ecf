/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
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

package org.eclipse.ecf.example.collab.share.io;

import java.io.File;
import java.io.Serializable;

public interface FileTransferListener extends Serializable {
	public void sendStart(FileTransferSharedObject obj, long length, float rate);

	public void sendData(FileTransferSharedObject obj, int dataLength);

	public void sendDone(FileTransferSharedObject obj, Exception e);

	public void receiveStart(FileTransferSharedObject obj, File aFile,
			long length, float rate);

	public void receiveData(FileTransferSharedObject obj, int dataLength);

	public void receiveDone(FileTransferSharedObject obj, Exception e);
}