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

package org.eclipse.ecf.presence.ui.chatroom;

/**
 * Listener for chat room view closing events.
 */
public interface IChatRoomViewCloseListener {
	/**
	 * If a non-<code>null</code> instance of this listener is provided to
	 * the {@link ChatRoomManagerView} this method will be called when the view
	 * is closing. 
	 * 
	 */
	public void chatRoomViewClosing();
}
