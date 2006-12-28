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
 * Chat message sender.
 */
public interface IChatMessageSender {

	/**
	 * Send chat message to given ID.
	 * 
	 * @param toID
	 *            the target receiver to receive the chat message. Must not be
	 *            null.
	 * @param message
	 *            the IChatMessage instance to send. Must not be null.
	 * 
	 * @throws ECFException
	 *             thrown if toID is null, message is null, or currently
	 *             disconnected
	 */
	public void sendChatMessage(ID toID, IChatMessage message)
			throws ECFException;

}
