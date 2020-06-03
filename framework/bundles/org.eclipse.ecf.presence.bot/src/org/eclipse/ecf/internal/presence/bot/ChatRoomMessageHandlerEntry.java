/****************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.presence.bot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ecf.presence.bot.IChatRoomMessageHandler;
import org.eclipse.ecf.presence.bot.IChatRoomMessageHandlerEntry;
import org.eclipse.ecf.presence.chatroom.IChatRoomMessage;

public class ChatRoomMessageHandlerEntry implements
		IChatRoomMessageHandlerEntry {

	private String expression;
	private IChatRoomMessageHandler handler;

	public ChatRoomMessageHandlerEntry(String expression,
			IChatRoomMessageHandler handler) {
		this.expression = expression;
		this.handler = handler;
	}

	public String getExpression() {
		return expression;
	}

	public IChatRoomMessageHandler getHandler() {
		return handler;
	}

	public void handleRoomMessage(IChatRoomMessage message) {
		if (expression == null || canExecute(message.getMessage()))
			handler.handleRoomMessage(message);
	}

	private boolean canExecute(String message) {
		Pattern pattern = Pattern.compile(getExpression(),
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(message);
		return matcher.matches();
	}

}
