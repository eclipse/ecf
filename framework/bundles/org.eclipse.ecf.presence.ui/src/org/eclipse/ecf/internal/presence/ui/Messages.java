/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.internal.presence.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.ecf.internal.presence.ui.messages"; //$NON-NLS-1$
	
	public static String ChatRoomManagerUI_EXCEPTION_CHAT_ROOM_VIEW_INITIALIZATION;
	public static String ChatRoomManagerUI_EXCEPTION_NO_ROOT_CHAT_ROOM_MANAGER;

	public static String MultiRosterView_SendIM;
	public static String MultiRosterView_Remove;
	public static String MultiRosterView_SetStatusAs;
	public static String MultiRosterView_SetAvailable;
	public static String MultiRosterView_SetAway;
	public static String MultiRosterView_SetDoNotDisturb;
	public static String MultiRosterView_SetInvisible;
	public static String MultiRosterView_SetOffline;
	public static String MultiRosterView_AddContact;

	public static String MessagesView_ShowTimestamps;
	public static String MessagesView_CouldNotSendMessage;
	public static String MessagesView_TypingNotification;
	
	public static String AddContactDialog_DialogTitle;
	public static String AddContactDialog_UserID;
	public static String AddContactDialog_Alias;
	public static String AddContactDialog_Account;
	
	public static String RosterWorkbenchAdapterFactory_Mode;
	public static String RosterWorkbenchAdapterFactory_Type;
	public static String RosterWorkbenchAdapterFactory_Account;
	public static String RosterWorkbenchAdapterFactory_Disconnected;
	public static String RosterWorkbenchAdapterFactory_GroupLabel;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
