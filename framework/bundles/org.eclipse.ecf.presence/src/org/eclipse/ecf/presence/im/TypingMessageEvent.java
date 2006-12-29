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

public class TypingMessageEvent implements ITypingMessageEvent {

	private static final long serialVersionUID = 6612754945575950442L;

	protected ID fromID;
	
	protected ITypingMessage typingMessage;
	
	public TypingMessageEvent(ID fromID, ITypingMessage message) {
		this.fromID = fromID;
		this.typingMessage = message;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.presence.im.ITypingMessageEvent#getTypingMessage()
	 */
	public ITypingMessage getTypingMessage() {
		return typingMessage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.presence.im.IIMMessageEvent#getFromID()
	 */
	public ID getFromID() {
		return fromID;
	}

}
