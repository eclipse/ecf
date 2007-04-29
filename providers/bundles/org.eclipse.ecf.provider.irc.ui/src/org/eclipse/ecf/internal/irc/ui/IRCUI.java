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

package org.eclipse.ecf.internal.irc.ui;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.util.IExceptionHandler;
import org.eclipse.ecf.presence.chatroom.IChatRoomManager;
import org.eclipse.ecf.presence.ui.chatroom.ChatRoomManagerUI;

/**
 *
 */
public class IRCUI extends ChatRoomManagerUI {

	public static final String CHANNEL_PREFIX = "#";
	
	/**
	 * @param container
	 * @param manager
	 */
	public IRCUI(IContainer container, IChatRoomManager manager) {
		super(container, manager);
	}

	public IRCUI(IContainer container, IChatRoomManager manager,
			IExceptionHandler exceptionHandler) {
		super(container,manager,exceptionHandler);
	}
	
	protected String modifyRoomNameForTarget(String roomName) {
		if (!roomName.startsWith(CHANNEL_PREFIX)) return new String(CHANNEL_PREFIX+roomName);
		return roomName;
	}
	
	protected String[] getRoomsForTarget() {
		String initialRooms = null;
		try {
			URI targetURI = new URI(targetID.getName());
			initialRooms = targetURI.getPath();
		} catch (URISyntaxException e) {
		}
		if (initialRooms == null || initialRooms.equals("")) //$NON-NLS-1$
			return new String[0];
		while (initialRooms.charAt(0) == '/')
			initialRooms = initialRooms.substring(1);
		
		if (initialRooms.startsWith(CHANNEL_PREFIX)) return new String [] { initialRooms };
		else return super.getRoomsForTarget();
	}

}
