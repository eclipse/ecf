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
import org.eclipse.ecf.core.identity.ID;

/**
 * @since 2.0
 */
public interface FileReceiverUI {

	public void receiveStart(ID from, File aFile, long length, float rate);

	public void receiveData(ID from, File aFile, int dataLength);

	public void receiveDone(ID from, File aFile, Exception e);

}
