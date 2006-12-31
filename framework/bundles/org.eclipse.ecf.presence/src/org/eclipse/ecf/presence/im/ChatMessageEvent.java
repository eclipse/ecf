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

/**
 * Chat message event class
 */
public class ChatMessageEvent implements IChatMessageEvent {

	private static final long serialVersionUID = 3813922441423314924L;

	protected ID fromID;
	protected IChatMessage message;

	public ChatMessageEvent(ID fromID, IChatMessage message) {
		this.fromID = fromID;
		this.message = message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.presence.im.IChatMessageEvent#getFromID()
	 */
	public ID getFromID() {
		return fromID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.presence.im.IChatMessageEvent#getMessage()
	 */
	public IChatMessage getChatMessage() {
		return message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer("ChatMessageEvent[");
		buf.append("fromID=").append(getFromID());
		buf.append(";message=").append(message).append("]");
		return buf.toString();
	}

}
