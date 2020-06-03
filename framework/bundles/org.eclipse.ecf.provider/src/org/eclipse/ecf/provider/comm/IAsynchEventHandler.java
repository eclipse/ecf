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
package org.eclipse.ecf.provider.comm;

import java.io.IOException;

/**
 * Callback interface for handling asynchronous connection events
 * 
 */
public interface IAsynchEventHandler extends IConnectionListener {
	/**
	 * Handle asynchronous connection event
	 * 
	 * @param event
	 *            the asynchronous connection event to handle
	 * @throws IOException
	 *             if connection event cannot be handled
	 */
	public void handleAsynchEvent(AsynchEvent event) throws IOException;
}