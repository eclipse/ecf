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

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;

/**
 * Typing message sender.
 */
public interface ITypingMessageSender {

	/**
	 * Send typing message to a remote receiver.
	 * 
	 * @param toID
	 *            the ID of the receiver of the typing message. Must not be
	 *            null.
	 * 
	 * @param isTyping
	 *            true if user is typing, false otherwise
	 * 
	 * @param body
	 * 		      the content of what has been/is being typed
     *
	 * @throws ECFException
	 *             thrown if toID or typingMessage parameters are null, or if
	 *             provider not currently connected.
	 */
	public void sendTypingMessage(ID toID, boolean isTyping, String body)
			throws ECFException;
	
}
