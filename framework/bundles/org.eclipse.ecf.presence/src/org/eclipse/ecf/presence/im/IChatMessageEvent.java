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

package org.eclipse.ecf.presence.im;

import org.eclipse.ecf.presence.IIMMessageEvent;

/**
 * Chat message event.
 */
public interface IChatMessageEvent extends IIMMessageEvent {

	/**
	 * Get chat message sent.
	 * 
	 * @return IChatMessage sent to this receiver. Will not be <code>null</code>.
	 */
	public IChatMessage getChatMessage();

	/**
	 * Get chat associated with this chat message event.
	 * 
	 * @return IChat associated with this chat message event.  May be <code>null</code> if
	 * provider does not support having an IChat.
	 */
	public IChat getChat();
}
