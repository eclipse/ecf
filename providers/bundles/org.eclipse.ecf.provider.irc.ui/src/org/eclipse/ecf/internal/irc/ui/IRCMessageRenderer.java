/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Jacek Pospychala <jacek.pospychala@pl.ibm.com> - bug 197329, 190851
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.irc.ui;

import org.eclipse.ecf.presence.ui.chatroom.MessageRenderer;

public class IRCMessageRenderer extends MessageRenderer {

	/**
	 * Messages sent by local user using /me command
	 */
	protected static final String IRC_ME_COLOR = "org.eclipse.ecf.presence.irc.ui.meColor"; //$NON-NLS-1$
	protected static final String IRC_ME_FONT = "org.eclipse.ecf.presence.irc.ui.meFont"; //$NON-NLS-1$
	
	
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
			append(message, IRC_ME_COLOR, null, IRC_ME_FONT);
		} else {
			super.appendNickname();
		}
	}
	
	protected void appendMessage() {
		if (isActionMessage) {
			append(message, IRC_ME_COLOR, null, IRC_ME_FONT);
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
