/****************************************************************************
 * Copyright (c) 2004, 2007 Composent, Inc. and others.
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
	 *            <code>null</code>.
	 * 
	 * @param isTyping
	 *            true if user is typing, false if they've stopped typing.
	 * 
	 * @param body
	 *            the content of what has been/is being typed. May be
	 *            <code>null</code>.
	 * 
	 * @throws ECFException
	 *             thrown if toID is <code>null</code>, or if provider not
	 *             currently connected.
	 */
	public void sendTypingMessage(ID toID, boolean isTyping, String body)
			throws ECFException;

}
