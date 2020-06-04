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
package org.eclipse.ecf.provider.irc.internal.bot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.presence.chatroom.IChatRoomMessageSender;
import org.eclipse.ecf.provider.irc.bot.handler.ICommandHandler;

public class CommandEntry implements ICommandEntry {

	private String expression;
	private ICommandHandler handler;
	private IChatRoomMessageSender sender;

	public CommandEntry(String expression, ICommandHandler handler) {
		this.expression = expression;
		this.handler = handler;
	}
	
	public String getExpression() {
		return expression;
	}

	public ICommandHandler getHandler() {
		return handler;
	}

	public void execute(String message, IChatRoomMessageSender sender) {
		if(canExecute(message)) {
			try {
				handler.execute(message, sender);
			} catch (ECFException e) {
				e.printStackTrace();
			}
		}
	}

	public IChatRoomMessageSender getSender() {
		return sender;
	}

	private boolean canExecute(String message) {
		Pattern pattern = Pattern.compile(getExpression(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(message);
		return matcher.matches();
	}

}
