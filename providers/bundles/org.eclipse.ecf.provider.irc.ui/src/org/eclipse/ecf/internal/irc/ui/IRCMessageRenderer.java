/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jacek Pospychala <jacek.pospychala@pl.ibm.com> - bug 197329
 *****************************************************************************/
package org.eclipse.ecf.internal.irc.ui;

import org.eclipse.ecf.presence.ui.chatroom.MessageRenderer;
import org.eclipse.swt.SWT;

public class IRCMessageRenderer extends MessageRenderer {

	private final static String ME_PREFIX = "\01ACTION "; //$NON-NLS-1$
	private final static String ME_SUFFIX = "\01"; //$NON-NLS-1$

	private boolean isActionMessage;
	
	protected void doRender() {
		String actionMessage = getActionMessage(message);
		isActionMessage = (actionMessage != null);
		
		if (isActionMessage) {
			message = actionMessage;
		}
		
		super.doRender();
	}

	protected void appendNickname() {
		if (isActionMessage) {
			String message = originator + " "; //$NON-NLS-1$
			append(message, null, null, SWT.ITALIC);
		} else {
			super.appendNickname();
		}
	}
	
	protected void appendMessage() {
		if (isActionMessage) {
			append(message, null, null, SWT.ITALIC);
		} else {
			super.appendMessage();
		}
	}
	
	private String getActionMessage(String message) {
		if (message.startsWith(ME_PREFIX) && message.endsWith(ME_SUFFIX)) {
			return message.substring(ME_PREFIX.length(), message.length() - ME_SUFFIX.length());
		} else {
			return null;
		}
	}

	
}
