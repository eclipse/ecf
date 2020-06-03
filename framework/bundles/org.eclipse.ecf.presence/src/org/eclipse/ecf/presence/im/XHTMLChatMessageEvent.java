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

public class XHTMLChatMessageEvent extends ChatMessageEvent implements IXHTMLChatMessageEvent {

	/**
	 * @param fromID
	 * @param message
	 */
	public XHTMLChatMessageEvent(ID fromID, IXHTMLChatMessage message) {
		super(fromID, message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.presence.im.IXHTMLChatMessageEvent#getXHTMLChatMessage()
	 */
	public IXHTMLChatMessage getXHTMLChatMessage() {
		return (IXHTMLChatMessage) super.getChatMessage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer("XHTMLChatMessageEvent["); //$NON-NLS-1$
		buf.append("fromID=").append(getFromID()); //$NON-NLS-1$
		buf.append(";xhtmlchatmessage=").append(getXHTMLChatMessage()).append( //$NON-NLS-1$
				"]"); //$NON-NLS-1$
		return buf.toString();
	}

}
