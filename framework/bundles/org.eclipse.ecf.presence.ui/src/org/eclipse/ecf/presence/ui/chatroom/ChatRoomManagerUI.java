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
package org.eclipse.ecf.presence.ui.chatroom;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerListener;
import org.eclipse.ecf.core.events.IContainerConnectedEvent;
import org.eclipse.ecf.core.events.IContainerDisconnectedEvent;
import org.eclipse.ecf.core.events.IContainerEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.IExceptionHandler;
import org.eclipse.ecf.internal.presence.ui.Activator;
import org.eclipse.ecf.internal.presence.ui.Messages;
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

/**
 * Chat room manager user interface.
 */
public class ChatRoomManagerUI {
	private static final String CHAT_ROOM_MANAGER_VIEWID = "org.eclipse.ecf.ui.views.ChatRoomManagerView"; //$NON-NLS-1$

	IContainer container;

	IChatRoomManager manager;

	boolean isContainerConnected = false;

	boolean viewAlreadyActive = false;

	IExceptionHandler exceptionHandler = null;

	ChatRoomManagerView chatroomview = null;

	ID targetID = null;

	public ChatRoomManagerUI(IContainer container, IChatRoomManager manager) {
		this(container, manager, null);
	}

	public ChatRoomManagerUI(IContainer container, IChatRoomManager manager,
			IExceptionHandler exceptionHandler) {
		super();
		this.container = container;
		this.manager = manager;
		this.exceptionHandler = exceptionHandler;
	}

	public ID getTargetID() {
		return targetID;
	}

	private void setupNewView() throws Exception {
		IChatRoomInfo roomInfo = manager.getChatRoomInfo(null);
		Assert.isNotNull(roomInfo,
				Messages.ChatRoomManagerUI_EXCEPTION_NO_ROOT_CHAT_ROOM_MANAGER);
		IChatRoomContainer chatRoom = roomInfo.createChatRoomContainer();
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
							String channel = getChannelForTarget();
							if (channel != null && !channel.equals(""))
								chatroomview.joinRoom(channel);
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

	/**
	 * Show a chat room manager UI for given targetID.  If a UI already
	 * exists that is connected to the given targetID, then it will be raised.
	 * and isContainerConnected
	 * connected to the given targetID then this will show the view associated
	 * with this targetID, and return <code>true</code>. The caller then
	 * <b>should not</b> connect the container, as there is already a container
	 * connected to the given target. If we are not already connected, then this
	 * method will return <code>false</code>, indicating that the caller
	 * should connect the new container to the given target ID.
	 * 
	 * @param targetID
	 */
	public void showForTarget(final ID targetID) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				try {
					ChatRoomManagerUI.this.targetID = targetID;
					chatroomview = getChatRoomManagerView();
					// If we're not already active, then setup new view
					if (!viewAlreadyActive) {
						setupNewView();
					} else if (isContainerConnected) {
						// If we are already active, and connected, then just
						// join room
						String channel = getChannelForTarget();
						if (channel != null && !channel.equals(""))
							chatroomview.joinRoom(channel);
						// We're already connected, so all we do is return
						return;
					}
				} catch (Exception e) {
					if (exceptionHandler != null)
						exceptionHandler.handleException(e);
					else
						Activator
								.getDefault()
								.getLog()
								.log(
										new Status(
												IStatus.ERROR,
												Activator.PLUGIN_ID,
												IStatus.ERROR,
												Messages.ChatRoomManagerUI_EXCEPTION_CHAT_ROOM_VIEW_INITIALIZATION
														+ targetID, e));
				}
			}

		});
	}

	public boolean isContainerConnected() {
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
		return uri.getAuthority() + ((port == -1) ? "" : ":" + port); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected ChatRoomManagerView getChatRoomManagerView()
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

	protected String getChannelForTarget() {
		String initialRoom = null;
		try {
			URI targetURI = new URI(targetID.getName());
			initialRoom = targetURI.getRawFragment();
		} catch (URISyntaxException e) {
		}
		if (initialRoom == null || initialRoom.equals("")) //$NON-NLS-1$
			return null;
		while (initialRoom.charAt(0) == '/')
			initialRoom = initialRoom.substring(1);
		if (initialRoom.charAt(0) != '#')
			initialRoom = "#" + initialRoom; //$NON-NLS-1$
		return initialRoom;
	}

}
