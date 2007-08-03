/****************************************************************************
 * Copyright (c) 2004, 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *    Jacek Pospychala <jacek.pospychala@pl.ibm.com> - bug 197329
 *****************************************************************************/

package org.eclipse.ecf.internal.irc.ui;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.StringTokenizer;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.IExceptionHandler;
import org.eclipse.ecf.presence.chatroom.IChatRoomContainer;
import org.eclipse.ecf.presence.chatroom.IChatRoomManager;
import org.eclipse.ecf.presence.ui.chatroom.ChatRoomManagerUI;
import org.eclipse.ecf.presence.ui.chatroom.IMessageRenderer;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;

/**
 * 
 */
public class IRCUI extends ChatRoomManagerUI {

	public static final String CHANNEL_PREFIX = "#"; //$NON-NLS-1$

	private static final String COMMAND_PREFIX = "/"; //$NON-NLS-1$

	private static final String COMMAND_DELIM = " "; //$NON-NLS-1$

	/**
	 * @param container
	 * @param manager
	 */
	public IRCUI(IContainer container, IChatRoomManager manager) {
		super(container, manager);
	}

	public IRCUI(IContainer container, IChatRoomManager manager,
			IExceptionHandler exceptionHandler) {
		super(container, manager, exceptionHandler);
	}

	protected String modifyRoomNameForTarget(String roomName) {
		if (!roomName.startsWith(CHANNEL_PREFIX))
			return new String(CHANNEL_PREFIX + roomName);
		return roomName;
	}

	protected String[] getRoomsForTarget() {
		String initialRooms = null;
		try {
			URI targetURI = new URI(targetID.getName());
			initialRooms = targetURI.getPath();
		} catch (URISyntaxException e) {
		}
		if (initialRooms == null 
				|| initialRooms.equals("") || initialRooms.equals("/")) //$NON-NLS-1$
			return new String[0];
		while (initialRooms.charAt(0) == '/')
			initialRooms = initialRooms.substring(1);

		if (initialRooms.startsWith(CHANNEL_PREFIX))
			return new String[] { initialRooms };
		else
			return super.getRoomsForTarget();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.presence.ui.chatroom.ChatRoomManagerUI#handleCommand(java.lang.String)
	 */
	public String handleCommand(IChatRoomContainer chatRoomContainer,
			String inputLine) {
		if ((inputLine != null && inputLine.startsWith(COMMAND_PREFIX))) {
			StringTokenizer st = new StringTokenizer(inputLine, COMMAND_DELIM);
			int countTokens = st.countTokens();
			String tokens[] = new String[countTokens];
			for (int i = 0; i < countTokens; i++)
				tokens[i] = st.nextToken();
			String command = tokens[0];
			while (command.startsWith(COMMAND_PREFIX))
				command = command.substring(1);
			// Look at first one and switch
			String[] args = new String[tokens.length - 1];
			System.arraycopy(tokens, 1, args, 0, tokens.length - 1);
			// JOIN can be done from root or channel
			if (command.equalsIgnoreCase(Messages.IRCUI_JOIN_COMMAND)) {
				chatroomview.joinRoom(manager.getChatRoomInfo(args[0]), (args.length > 1)?args[1]:"");
				return null;
			}
			// QUIT can be done from root or channel
			if (command.equalsIgnoreCase(Messages.IRCUI_QUIT_COMMAND)) {
				ID connectedID = container.getConnectedID();
				if (connectedID != null
						&& MessageDialog.openQuestion(chatroomview.getSite()
								.getShell(), Messages.IRCUI_DISCONNECT_CONFIRM_TITLE, NLS
								.bind(Messages.IRCUI_DISCONNECT_CONFIRM_MESSAGE, connectedID
										.getName())))
					chatroomview.disconnect();
				return null;
			}
			if (chatRoomContainer != null
					&& command.equalsIgnoreCase(Messages.IRCUI_PART_COMMAND)
					&& MessageDialog.openQuestion(chatroomview.getSite()
							.getShell(), Messages.IRCUI_DEPART_CONFIRM_TITLE, NLS.bind(
							Messages.IRCUI_DEPART_CONFIRM_MESSAGE, chatRoomContainer
									.getConnectedID().getName()))) {
				chatRoomContainer.disconnect();
				return null;
			}
		}
		return inputLine;

	}
	
	protected IMessageRenderer getDefaultMessageRenderer() {
		return new IRCMessageRenderer();
	}
}
