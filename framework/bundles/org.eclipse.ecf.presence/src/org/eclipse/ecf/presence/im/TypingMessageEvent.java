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

import org.eclipse.ecf.core.identity.ID;

public class TypingMessageEvent implements ITypingMessageEvent {

	protected ID fromID;

	protected ITypingMessage typingMessage;

	public TypingMessageEvent(ID fromID, ITypingMessage message) {
		this.fromID = fromID;
		this.typingMessage = message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.presence.im.ITypingMessageEvent#getTypingMessage()
	 */
	public ITypingMessage getTypingMessage() {
		return typingMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.presence.im.IIMMessageEvent#getFromID()
	 */
	public ID getFromID() {
		return fromID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer("TypingMessage["); //$NON-NLS-1$
		buf.append("fromID=").append(getFromID()); //$NON-NLS-1$
		buf.append(";typingMessage=").append(getTypingMessage()).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		return buf.toString();
	}

}
