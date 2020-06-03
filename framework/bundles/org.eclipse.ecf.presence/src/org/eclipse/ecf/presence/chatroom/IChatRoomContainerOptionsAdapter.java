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
package org.eclipse.ecf.presence.chatroom;

/**
 * Adapter interface for {@link IChatRoomManager} allowing options to be set for
 * chat room containers managed by manager
 * 
 */
public interface IChatRoomContainerOptionsAdapter {
	/**
	 * Set encoding for chat room manager that supports IChatRoomOptions
	 * 
	 * @param encoding
	 *            Must not be <code>null</code>.
	 * @return true if encoding set properly, false if encoding cannot be
	 *         set/reset
	 */
	public boolean setEncoding(String encoding);
}
