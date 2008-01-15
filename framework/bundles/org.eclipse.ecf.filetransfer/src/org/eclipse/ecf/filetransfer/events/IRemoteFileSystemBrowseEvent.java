/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.filetransfer.events;

import org.eclipse.ecf.filetransfer.IRemoteFile;

/**
 * Event that indicates that a directory list is available via {@link #getRemoteFiles()}.
 */
public interface IRemoteFileSystemBrowseEvent extends IRemoteFileSystemEvent {

	/**
	 * Get the list of files associated with this browse event.
	 * @return IRemoteFile[] the array of remote files for the given browse.
	 */
	public IRemoteFile[] getRemoteFiles();

}
