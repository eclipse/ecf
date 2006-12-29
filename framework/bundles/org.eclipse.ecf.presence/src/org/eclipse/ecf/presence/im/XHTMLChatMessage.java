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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ecf.core.identity.ID;

/**
 * XHTML chat message.
 * 
 */
public class XHTMLChatMessage extends ChatMessage implements IXHTMLChatMessage {

	private static final long serialVersionUID = -1322099958260366438L;
	
	protected List xhtmlbodies;
	
	/**
	 * @param fromID
	 * @param threadID
	 * @param type
	 * @param subject
	 * @param body
	 * @param xhtmlbodies
	 */
	public XHTMLChatMessage(ID fromID, ID threadID, Type type, String subject,
			String body, List xhtmlbodies) {
		super(fromID, threadID, type, subject, body);
		this.xhtmlbodies = (xhtmlbodies == null)?new ArrayList():xhtmlbodies;
	}

	/**
	 * @param fromID
	 * @param threadID
	 * @param subject
	 * @param body
	 * @param xhtmlbodies
	 */
	public XHTMLChatMessage(ID fromID, ID threadID, String subject, String body, List xhtmlbodies) {
		this(fromID, threadID, IChatMessage.Type.CHAT, subject, body, xhtmlbodies);
	}

	/**
	 * @param fromID
	 * @param type
	 * @param subject
	 * @param body
	 * @param xhtmlbodies
	 */
	public XHTMLChatMessage(ID fromID, Type type, String subject, String body, List xhtmlbodies) {
		this(fromID, null, type, subject, body, xhtmlbodies);
	}

	/**
	 * @param fromID
	 * @param subject
	 * @param body
	 * @param xhtmlbodies
	 */
	public XHTMLChatMessage(ID fromID, String subject, String body, List xhtmlbodies) {
		this(fromID, (ID) null, subject, body, xhtmlbodies);
	}

	/**
	 * @param fromID
	 * @param body
	 * @param xhtmlbodies
	 */
	public XHTMLChatMessage(ID fromID, String body, List xhtmlbodies) {
		this(fromID, (String) null, body, xhtmlbodies);
	}

	public XHTMLChatMessage(ID fromID, List xhtmlbodies) {
		this(fromID, (String) null, xhtmlbodies);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.presence.im.IXHTMLChatMessage#getXTHMLBodies()
	 */
	public List getXTHMLBodies() {
		return xhtmlbodies;
	}

}
