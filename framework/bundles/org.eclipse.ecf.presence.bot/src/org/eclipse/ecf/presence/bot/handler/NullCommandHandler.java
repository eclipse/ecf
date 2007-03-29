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

package org.eclipse.ecf.presence.bot.handler;

import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.presence.chatroom.IChatRoomMessage;
import org.eclipse.ecf.presence.chatroom.IChatRoomMessageSender;

/**
 *
 */
public class NullCommandHandler implements ICommandHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.presence.bot.handler.ICommandHandler#execute(org.eclipse.ecf.presence.chatroom.IChatRoomMessage, org.eclipse.ecf.presence.chatroom.IChatRoomMessageSender)
	 */
	public void execute(IChatRoomMessage message, IChatRoomMessageSender sender)
			throws ECFException {
		System.out.println("NullCommandHandler.execute(message="+message+",sender="+sender+")");
	}

}
