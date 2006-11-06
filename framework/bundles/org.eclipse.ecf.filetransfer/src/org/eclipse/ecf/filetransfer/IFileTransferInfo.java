/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.filetransfer;

import java.io.File;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ecf.filetransfer.events.IFileTransferRequestEvent;

/**
 * File transfer information delivered to
 * {@link IIncomingFileTransferRequestListener} via an event implementing
 * {@link IFileTransferRequestEvent#getFileTransferInfo()}
 * 
 */
public interface IFileTransferInfo extends IAdaptable {
	/**
	 * Get the file for the proposed file transfer
	 * 
	 * @return the proposed file. Will not return null.
	 */
	public File getFile();

	/**
	 * Get any properties associated with this file transfer. The map keys and
	 * values are assumed to be Strings.
	 * 
	 * @return Map of properties associated with this file transfer info. Will
	 *         not return null.
	 */
	public Map getProperties();

	/**
	 * Get any description associated with this file transfer info.
	 * 
	 * @return String description. May be null if no description provided.
	 */
	public String getDescription();

}
