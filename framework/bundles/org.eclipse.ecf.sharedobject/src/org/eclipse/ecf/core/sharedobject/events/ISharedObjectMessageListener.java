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
package org.eclipse.ecf.core.sharedobject.events;

/**
 * Listener for receiving shared object messages
 * 
 */
public interface ISharedObjectMessageListener {
	/**
	 * Receive shared object message via shared object message event.
	 * 
	 * @param event
	 *            the shared object message event holding the shared object
	 *            message
	 */
	public void handleSharedObjectMessage(ISharedObjectMessageEvent event);
}
