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

import org.eclipse.ecf.presence.bot.IIMMessageHandler;
import org.eclipse.ecf.presence.bot.IIMMessageHandlerEntry;
import org.eclipse.ecf.presence.im.IChatMessage;

public class IMMessageHandlerEntry implements IIMMessageHandlerEntry {

	private String expression;
	private IIMMessageHandler handler;

	public IMMessageHandlerEntry(String expression, IIMMessageHandler handler) {
		this.expression = expression;
		this.handler = handler;
	}

	public String getExpression() {
		return expression;
	}

	public IIMMessageHandler getHandler() {
		return handler;
	}

	public void handleIMMessage(IChatMessage message) {
		if (expression == null || canExecute(message.getBody()))
			handler.handleIMMessage(message);
	}

	private boolean canExecute(String message) {
		Pattern pattern = Pattern.compile(getExpression(),
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(message);
		return matcher.matches();
	}

}
