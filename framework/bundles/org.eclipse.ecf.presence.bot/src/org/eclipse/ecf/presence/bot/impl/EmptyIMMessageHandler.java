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

package org.eclipse.ecf.presence.bot.impl;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.presence.bot.IIMBotEntry;
import org.eclipse.ecf.presence.bot.IIMMessageHandler;
import org.eclipse.ecf.presence.im.IChatMessage;

/**
 * Default im message handler that does nothing in response to notifications.
 */
public class EmptyIMMessageHandler implements IIMMessageHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.presence.bot.handler.IIMMessageHandler#handleRoomMessage(org.eclipse.ecf.presence.im.IChatMessage)
	 */
	public void handleIMMessage(IChatMessage message) {
		System.out.println("handleIMMessage(" + message + ")");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.presence.bot.handler.IContainerAdvisor#preContainerConnect(org.eclipse.ecf.core.identity.ID)
	 */
	public void preContainerConnect(IContainer container, ID targetID) {
		System.out.println("preContainerConnect("+container+","+targetID+")");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.presence.bot.IIMMessageHandler#initRobot(org.eclipse.ecf.presence.bot.IIMBotEntry)
	 */
	public void init(IIMBotEntry robot) {
		System.out.println("init("+robot+")");
	}

}
