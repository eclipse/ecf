/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.filetransfer;

import org.eclipse.ecf.filetransfer.events.IFileTransferEvent;

/**
 * Listener for handling file transfer events. Instances implementing this
 * interface or sub-interfaces will have their handleTransferEvent called
 * asynchronously when a given event is received. Implementers must be prepared
 * to have this method called asynchronously by an arbitrary thread.
 * 
 */
public interface IFileTransferListener {
	/**
	 * Handle file transfer events
	 * 
	 * @param event
	 *            the event to be handled. should not be <code>null</code>.
	 */
	public void handleTransferEvent(IFileTransferEvent event);
}
