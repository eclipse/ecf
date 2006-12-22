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
package org.eclipse.ecf.example.collab;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.presence.IMessageListener;
import org.eclipse.ecf.presence.IPresence;
import org.eclipse.ecf.presence.chat.IChatRoomMessageSender;
import org.eclipse.ecf.presence.chat.IChatParticipantListener;
import org.eclipse.ecf.presence.chat.IChatRoomContainer;
import org.eclipse.ecf.presence.chat.IChatRoomManager;
import org.eclipse.ecf.presence.chat.IChatRoomInfo;
import org.eclipse.ecf.ui.views.ChatRoomView;
import org.eclipse.ecf.ui.views.IChatRoomViewCloseListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class ChatRoomManagerUI {
	IChatRoomManager manager;
	
	public ChatRoomManagerUI(IChatRoomManager manager) {
		super();
		this.manager = manager;
	}

	public ChatRoomManagerUI setup(final IContainer newClient, final ID targetID,
			String username) {
		// If we don't already have a connection/view to this room then
		// now, create chat room instance
		// Get the chat message sender callback so that we can send
		// messages to chat room
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
				try {
					IChatRoomInfo roomInfo = manager.getChatRoomInfo(targetID.getName());
					IChatRoomContainer chatRoom = null;
					try {
						chatRoom = roomInfo.createChatRoomContainer();
					} catch (ContainerCreateException e1) {
						// can't happen
					}
					IChatRoomMessageSender sender = chatRoom.getChatMessageSender();
					IWorkbenchWindow ww = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow();
					IWorkbenchPage wp = ww.getActivePage();
					IViewPart view = wp.showView("org.eclipse.ecf.ui.views.ChatRoomView");
					final ChatRoomView chatroomview = (ChatRoomView) view;
					// initialize the chatroomview with the necessary
					// information
					chatroomview.initialize(new IChatRoomViewCloseListener() {
						public void chatRoomViewClosing(String secondaryID) {
							newClient.dispose();
						}
					}, null, chatRoom, roomInfo, sender);
					// Add listeners so that the new chat room gets
					// asynch notifications of various relevant chat room events
					chatRoom.addMessageListener(new IMessageListener() {
						public void handleMessage(ID fromID, ID toID, Type type,
								String subject, String messageBody) {
							chatroomview.handleMessage(fromID, toID, type, subject,
									messageBody);
						}
					});
					chatRoom.addChatParticipantListener(new IChatParticipantListener() {
						public void handlePresence(ID fromID, IPresence presence) {
							chatroomview.handlePresence(fromID, presence);
						}
						public void handleArrivedInChat(ID participant) {
							chatroomview.handleJoin(participant);
						}
						public void handleDepartedFromChat(ID participant) {
							chatroomview.handleLeave(participant);
						}
					});
				} catch (PartInitException e) {
					ClientPlugin.getDefault().getLog().log(new Status(IStatus.ERROR,ClientPlugin.PLUGIN_ID,0,
							"Exception in chat room view initialization for chat room "
									+ targetID, e));
				}
            }
        });
		return this;
	}
}
