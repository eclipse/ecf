/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.internal.presence.bot;

import java.util.List;

import org.eclipse.ecf.presence.bot.IChatRoomBotEntry;

public class ChatRoomBotEntry implements IChatRoomBotEntry {

	private String id;
	private String name;
	private String containerFactoryName;
	private String connectID;
	private String password;
	private String chatRoom;
	private String chatRoomPassword;
	private List commands;
	
	public ChatRoomBotEntry(String id, String name, String containerFactoryName, String connectID, String password, String chatRoom, String chatRoomPassword, List commands) {
		this.id = id;
		this.name = name;
		this.containerFactoryName = containerFactoryName;
		this.connectID = connectID;
		this.password = password;
		this.chatRoom = chatRoom;
		this.chatRoomPassword = chatRoomPassword;
		this.commands = commands;
	}
	
	public List getCommands() {
		return commands;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.internal.presence.bot.IChatRoomBotEntry#getChatRoom()
	 */
	public String getChatRoom() {
		return chatRoom;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.internal.presence.bot.IChatRoomBotEntry#getConnectID()
	 */
	public String getConnectID() {
		return connectID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.internal.presence.bot.IChatRoomBotEntry#getContainerFactoryName()
	 */
	public String getContainerFactoryName() {
		return containerFactoryName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.internal.presence.bot.IChatRoomBotEntry#getPassword()
	 */
	public String getPassword() {
		return password;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.internal.presence.bot.IChatRoomBotEntry#getChatRoomPassword()
	 */
	public String getChatRoomPassword() {
		return chatRoomPassword;
	}

}
