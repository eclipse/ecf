/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Jacek Pospychala <jacek.pospychala@pl.ibm.com> - bug 192762
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.irc.ui.actions;

import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.user.IUser;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.internal.irc.ui.Activator;
import org.eclipse.ecf.presence.chatroom.IChatRoomContainer;
import org.eclipse.ecf.presence.ui.chatroom.ChatRoomManagerView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.statushandlers.StatusManager;

abstract public class AbstractActionDelegate implements IViewActionDelegate {

	private IStructuredSelection selection;
	private IChatRoomContainer chatRoomContainer;

	public AbstractActionDelegate() {
		super();
	}

	protected abstract String getMessage(String username);
	
	protected String getUsername(IUser user) {
		String username = user.getName();
		if (username.startsWith("@")) { //$NON-NLS-1$
			username = username.substring(1);
		}
		return username;
	}
	
	public void run(IAction action) {
		if ((chatRoomContainer == null) || (selection == null)) {
			return;
		}
		try {
			Iterator iterator = selection.iterator();
			while (iterator.hasNext()) {
				IUser user = (IUser) iterator.next();
				String message = getMessage(getUsername(user));
				chatRoomContainer.getChatRoomMessageSender().sendMessage(message);
			}
		} catch (ECFException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
		}
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		if (! (selection instanceof IStructuredSelection)) {
			return;
		}
		
		this.selection = (IStructuredSelection) selection;
	}

	public void init(IViewPart view) {
		chatRoomContainer = ((ChatRoomManagerView) view).getActiveChatRoomContainer();
	}

}
