/****************************************************************************
 * Copyright (c) 2009 IBM, and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   IBM Corporation - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.filetransfer.events.socket;

import org.eclipse.core.runtime.IAdaptable;

// IFileTransfer or IRemoteFileSystemRequest, other?
public interface ISocketEventSource extends IAdaptable {
	void addListener(ISocketListener listener);

	void removeListener(ISocketListener listener);

	void fireEvent(ISocketEvent event);
}
