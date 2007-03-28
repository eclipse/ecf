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

public class ChatBotEntry implements IChatBotEntry {

	private String id;
	private String name;
	private String containerFactoryName;
	private String connectID;
	private String password;
	private String chatRoom;
	private List commands;
	
	public ChatBotEntry(String id, String name, String containerFactoryName, String connectID, String password, String chatRoom, List commands) {
		this.id = id;
		this.name = name;
		this.containerFactoryName = containerFactoryName;
		this.connectID = connectID;
		this.password = password;
		this.chatRoom = chatRoom;
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
	 * @see org.eclipse.ecf.internal.presence.bot.IChatBotEntry#getChatRoom()
	 */
	public String getChatRoom() {
		return chatRoom;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.internal.presence.bot.IChatBotEntry#getConnectID()
	 */
	public String getConnectID() {
		return connectID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.internal.presence.bot.IChatBotEntry#getContainerFactoryName()
	 */
	public String getContainerFactoryName() {
		return containerFactoryName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.internal.presence.bot.IChatBotEntry#getPassword()
	 */
	public String getPassword() {
		return password;
	}

}
