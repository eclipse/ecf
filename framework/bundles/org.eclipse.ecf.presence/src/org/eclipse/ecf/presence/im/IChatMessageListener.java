/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.presence.im;

/**
 * Chat message listener
 */
public interface IChatMessageListener {

	/**
	 * Handle chat message event (reception of chat message).
	 * 
	 * @param chatMessageEvent
	 *            the event instance to handle. Will not be null.
	 */
	public void handleChatMessageEvent(IChatMessageEvent chatMessageEvent);

}
