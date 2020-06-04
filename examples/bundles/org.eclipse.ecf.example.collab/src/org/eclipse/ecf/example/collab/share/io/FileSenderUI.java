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

/**
 * @since 2.0
 */
public interface FileSenderUI {

	public void sendStart(File aFile, long length, float rate);

	public void sendData(File aFile, long dataLength);

	public void sendDone(File aFile, Exception e);

}
