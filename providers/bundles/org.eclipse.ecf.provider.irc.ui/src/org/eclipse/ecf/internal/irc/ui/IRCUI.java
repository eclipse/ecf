/****************************************************************************
 * Copyright (c) 2004, 2007 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *    Jacek Pospychala <jacek.pospychala@pl.ibm.com> - bug 197329
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.internal.irc.ui;

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

	public IRCUI(IContainer container, IChatRoomManager manager, IExceptionHandler exceptionHandler) {
		super(container, manager, exceptionHandler);
	}

	protected String modifyRoomNameForTarget(String roomName) {
		if (!roomName.startsWith(CHANNEL_PREFIX))
			return new String(CHANNEL_PREFIX + roomName);
		return roomName;
	}

	protected String[] getRoomsForTarget() {
		String initialChannels = targetID.getName();
		final int protocolSeparator = initialChannels.indexOf("://"); //$NON-NLS-1$
		if (protocolSeparator != -1)
			initialChannels = initialChannels.substring(protocolSeparator + 3);
		final int index = initialChannels.lastIndexOf("/"); //$NON-NLS-1$
		if (index != -1) {
			initialChannels = initialChannels.substring(index + 1);
			while (initialChannels.startsWith("/"))initialChannels = initialChannels.substring(1); //$NON-NLS-1$
		} else
			initialChannels = null;
		if (initialChannels == null || initialChannels.equals("") || initialChannels.equals("/")) //$NON-NLS-1$ //$NON-NLS-2$
			return new String[0];

		final StringTokenizer toks = new StringTokenizer(initialChannels, ROOM_DELIMITER);
		final String[] results = new String[toks.countTokens()];
		for (int i = 0; i < results.length; i++) {
			String tmp = toks.nextToken();
			final StringBuffer buf = new StringBuffer();
			while (tmp.startsWith("%23")) { //$NON-NLS-1$
				buf.append("#"); //$NON-NLS-1$
				tmp = tmp.substring(3);
			}
			buf.append(tmp);
			results[i] = buf.toString();
			if (!results[i].startsWith("#"))results[i] = "#" + results[i]; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return results;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.presence.ui.chatroom.ChatRoomManagerUI#handleCommand(java.lang.String)
	 */
	public String handleCommand(IChatRoomContainer chatRoomContainer, String inputLine) {
		if ((inputLine != null && inputLine.startsWith(COMMAND_PREFIX))) {
			final StringTokenizer st = new StringTokenizer(inputLine, COMMAND_DELIM);
			final int countTokens = st.countTokens();
			final String tokens[] = new String[countTokens];
			for (int i = 0; i < countTokens; i++)
				tokens[i] = st.nextToken();
			String command = tokens[0];
			while (command.startsWith(COMMAND_PREFIX))
				command = command.substring(1);
			// Look at first one and switch
			final String[] args = new String[tokens.length - 1];
			System.arraycopy(tokens, 1, args, 0, tokens.length - 1);
			// JOIN can be done from root or channel
			if (command.equalsIgnoreCase(Messages.IRCUI_JOIN_COMMAND)) {
				chatroomview.joinRoom(manager.getChatRoomInfo(args[0]), (args.length > 1) ? args[1] : ""); //$NON-NLS-1$
				return null;
			}
			// QUIT can be done from root or channel
			if (command.equalsIgnoreCase(Messages.IRCUI_QUIT_COMMAND)) {
				final ID connectedID = container.getConnectedID();
				if (connectedID != null && MessageDialog.openQuestion(chatroomview.getSite().getShell(), Messages.IRCUI_DISCONNECT_CONFIRM_TITLE, NLS.bind(Messages.IRCUI_DISCONNECT_CONFIRM_MESSAGE, connectedID.getName())))
					chatroomview.disconnect();
				return null;
			}
			if (chatRoomContainer != null && command.equalsIgnoreCase(Messages.IRCUI_PART_COMMAND) && MessageDialog.openQuestion(chatroomview.getSite().getShell(), Messages.IRCUI_DEPART_CONFIRM_TITLE, NLS.bind(Messages.IRCUI_DEPART_CONFIRM_MESSAGE, chatRoomContainer.getConnectedID().getName()))) {
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
