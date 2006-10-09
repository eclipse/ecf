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
package org.eclipse.ecf.presence;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.presence.IMessageListener.Type;

/**
 * Interface for sending text messages (IM) between users.
 * 
 * @author slewis
 * 
 */
public interface IMessageSender {
	/**
	 * Send text message
	 * 
	 * @param fromID
	 *            the user id of the sender. Cannot be null
	 * @param toID
	 *            the user id of the target receiver of the message. Cannot be
	 *            null
	 * @param type
	 *            the Type of the message
	 * @param subject
	 *            the subject of the message
	 * @param messageBody
	 *            the message body
	 */
	public void sendMessage(ID fromID, ID toID, Type type, String subject,
			String messageBody);
}
