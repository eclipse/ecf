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

package org.eclipse.ecf.presence.chatroom;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.presence.IMMessage;

/**
 * Chat room message implementation class.
 */
public class ChatRoomMessage extends IMMessage implements IChatRoomMessage {

	private static final long serialVersionUID = -5099099538044060019L;

	protected String message;

	/**
	 * @param fromID
	 *            the sender ID
	 * @param message
	 *            the message sent.
	 */
	public ChatRoomMessage(ID fromID, String message) {
		super(fromID);
		this.message = message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.presence.chatroom.IChatRoomMessage#getMessage()
	 */
	public String getMessage() {
		return message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer("ChatRoomMessage["); //$NON-NLS-1$
		buf.append("fromID=").append(getFromID()); //$NON-NLS-1$
		buf.append(";message=").append(message).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		return buf.toString();
	}

}
