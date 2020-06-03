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

import org.eclipse.ecf.presence.IIMMessageEvent;

/**
 * Chat room message event.
 */
public interface IChatRoomMessageEvent extends IIMMessageEvent {

	/**
	 * Get chat room message sent.
	 * 
	 * @return IChatRoomMessage sent to this receiver. Will not be
	 *         <code>null</code>.
	 */
	public IChatRoomMessage getChatRoomMessage();

}
