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
import org.eclipse.ecf.core.util.ECFException;

/**
 * Interface for sending text messages (IM) between users. Access to instances
 * implementing this interface is provided by
 * {@link IPresenceContainerAdapter#getMessageSender()}
 * 
 * @see IPresenceContainerAdapter
 */
public interface IMessageSender {
	/**
	 * Send text message
	 * @param toID
	 *            the user id of the target receiver of the message. Cannot be
	 *            null
	 * @param subject
	 *            the subject of the message
	 * @param body
	 *            the message body
	 * @exception ECFException
	 *                thrown if message cannot be sent (e.g. because of previous
	 *                disconnection)
	 */
	public void sendMessage(ID toID, String subject, String body) throws ECFException;
}
