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
 * Typing Message implementation class
 */
public class TypingMessage extends IMMessage implements ITypingMessage {

	private static final long serialVersionUID = 6534377119279656830L;

	protected boolean typing = false;
	
	protected String body = "";
	
	public TypingMessage(ID fromID, boolean typing, String body) {
		super(fromID);
		this.typing = typing;
		this.body = body;
	}
	
	public TypingMessage(ID fromID, String body) {
		super(fromID);
		if (body != null) {
			this.typing = true;
			this.body = body;
		} else {
			this.typing = false;
		}
	}
	
	public TypingMessage(ID fromID) {
		this(fromID,true,"");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.presence.im.ITypingMessage#getBody()
	 */
	public String getBody() {
		return body;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.presence.im.ITypingMessage#isTyping()
	 */
	public boolean isTyping() {
		return typing;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

}
