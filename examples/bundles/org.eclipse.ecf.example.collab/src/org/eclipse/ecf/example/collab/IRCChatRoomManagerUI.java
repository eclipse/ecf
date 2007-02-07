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

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerListener;
import org.eclipse.ecf.core.events.IContainerConnectedEvent;
import org.eclipse.ecf.core.events.IContainerDisconnectedEvent;
import org.eclipse.ecf.core.events.IContainerEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.presence.IIMMessageEvent;
import org.eclipse.ecf.presence.IIMMessageListener;
import org.eclipse.ecf.presence.chatroom.IChatRoomContainer;
import org.eclipse.ecf.presence.chatroom.IChatRoomInfo;
import org.eclipse.ecf.presence.chatroom.IChatRoomManager;
import org.eclipse.ecf.presence.chatroom.IChatRoomMessage;
import org.eclipse.ecf.presence.chatroom.IChatRoomMessageEvent;
import org.eclipse.ecf.ui.views.ChatRoomManagerView;
import org.eclipse.ecf.ui.views.IChatRoomViewCloseListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class IRCChatRoomManagerUI {
	private static final String CHAT_ROOM_MANAGER_VIEWID = "org.eclipse.ecf.ui.views.ChatRoomManagerView";

	IChatRoomManager manager;

	boolean isContainerConnected = false;

	boolean viewAlreadyActive = false;

	public IRCChatRoomManagerUI(IChatRoomManager manager) {
		super();
		this.manager = manager;
	}

	private void setupNewView(final IContainer container, final ID targetID,
			final ChatRoomManagerView chatroomview) {
		IChatRoomInfo roomInfo = manager.getChatRoomInfo(null);
		if (roomInfo == null)
			throw new NullPointerException(
					"Chat room manager does not expose chat room interface.  Cannot create UI");
		IChatRoomContainer chatRoom = null;
		try {
			chatRoom = roomInfo.createChatRoomContainer();
		} catch (ContainerCreateException e1) {
			// can't happen for 'root' roomInfo
		}
		// initialize the chatroomview with the necessary
		// information
		chatroomview.initialize(new IChatRoomViewCloseListener() {
			public void chatRoomViewClosing(String secondaryID) {
				container.dispose();
			}
		}, chatRoom, manager, targetID, chatRoom.getChatRoomMessageSender());
		// Add listener for container, so that if the container is spontaneously
		// disconnected,
		// then we will be able to have the UI respond by making itself inactive
		container.addListener(new IContainerListener() {
			public void handleEvent(final IContainerEvent evt) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						if (evt instanceof IContainerDisconnectedEvent) {
							IContainerDisconnectedEvent cd = (IContainerDisconnectedEvent) evt;
							final ID departedContainerID = cd.getTargetID();
							ID connectedID = targetID;
							if (connectedID == null
									|| connectedID.equals(departedContainerID)) {
								chatroomview.disconnected();
								isContainerConnected = false;
							}
						} else if (evt instanceof IContainerConnectedEvent) {
							isContainerConnected = true;
							chatroomview.setEnabled(true);
							chatroomview.joinRoom(getChannelFromID(targetID));
						}
					}
				});
			}
		});
		// Add listeners so that the new chat room gets
		// asynch notifications of various relevant chat room events
		chatRoom.addMessageListener(new IIMMessageListener() {
			public void handleMessageEvent(IIMMessageEvent messageEvent) {
				if (messageEvent instanceof IChatRoomMessageEvent) {
					IChatRoomMessage m = ((IChatRoomMessageEvent) messageEvent)
							.getChatRoomMessage();
					chatroomview.handleMessage(m.getFromID(), m.getMessage());
				}
			}
		});
	}

	public boolean isAlreadyConnectedToTarget(final IContainer container,
			final ID targetID, String username) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				try {
					final ChatRoomManagerView chatroomview = getChatRoomManagerView(targetID);
					// If we're not already active, then setup new view
					if (!viewAlreadyActive) {
						setupNewView(container, targetID, chatroomview);
					} else if (isContainerConnected) {
						// If we are already active, and connected, then just
						// join room
						chatroomview.joinRoom(getChannelFromID(targetID));
						// We're already connected, so all we do is return
						return;
					}
				} catch (Exception e) {
					ClientPlugin.getDefault().getLog().log(
							new Status(IStatus.ERROR, ClientPlugin.PLUGIN_ID,
									0,
									"Exception in chat room view initialization for "
											+ targetID, e));
				}
			}

		});
		return isContainerConnected;
	}

	protected boolean isContainerConnected() {
		return isContainerConnected;
	}

	protected String getSecondaryViewID(ID targetID) {
		URI uri;
		try {
			uri = new URI(targetID.getName());
		} catch (URISyntaxException e) {
			return null;
		}
		// Get authority, host, and port to define view ID
		int port = uri.getPort();
		return uri.getAuthority() + ((port == -1) ? "" : ":" + port);
	}

	protected ChatRoomManagerView getChatRoomManagerView(ID targetID)
			throws PartInitException {
		// Get view
		String secondaryViewID = getSecondaryViewID(targetID);
		IWorkbenchWindow ww = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage wp = ww.getActivePage();
		ChatRoomManagerView view = null;
		if (secondaryViewID == null)
			view = (ChatRoomManagerView) wp.showView(CHAT_ROOM_MANAGER_VIEWID);
		else {
			IViewReference viewRef = wp.findViewReference(
					CHAT_ROOM_MANAGER_VIEWID, secondaryViewID);
			if (viewRef == null)
				view = (ChatRoomManagerView) wp.showView(
						CHAT_ROOM_MANAGER_VIEWID, secondaryViewID,
						IWorkbenchPage.VIEW_ACTIVATE);
			else {
				// Old view with same secondaryViewID found, so use/restore it
				// rather than creating new view
				view = (ChatRoomManagerView) viewRef.getView(true);
			}
		}
		if (view.isEnabled())
			viewAlreadyActive = true;
		else
			viewAlreadyActive = false;
		return view;
	}

	protected String getChannelFromID(ID targetID) {
		String initialRoom = null;
		try {
			URI targetURI = new URI(targetID.getName());
			initialRoom = targetURI.getRawFragment();
		} catch (URISyntaxException e) {
		}
		if (initialRoom == null || initialRoom.equals(""))
			return null;
		while (initialRoom.charAt(0) == '/')
			initialRoom = initialRoom.substring(1);
		if (initialRoom.charAt(0) != '#')
			initialRoom = "#" + initialRoom;
		return initialRoom;
	}

}
