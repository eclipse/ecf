/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.example.clients;

import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.presence.IIMMessageEvent;
import org.eclipse.ecf.presence.IIMMessageListener;
import org.eclipse.ecf.presence.chatroom.IChatRoomContainer;
import org.eclipse.ecf.presence.chatroom.IChatRoomMessage;
import org.eclipse.ecf.presence.chatroom.IChatRoomMessageEvent;
import org.eclipse.ecf.presence.chatroom.IChatRoomMessageSender;

/**
 * To be started as an application. Go to Run->Run..., create a new Eclipse
 * Application, select org.eclipse.ecf.example.clients.robot as the application
 * and make sure you have all required plug-ins.
 * 
 */
public class RobotApplication implements IPlatformRunnable, IMessageReceiver,
		IIMMessageListener {

	private IChatRoomMessageSender sender;

	private boolean running = false;

	private String userName;

	public synchronized Object run(Object args) throws Exception {
		if (args instanceof Object[]) {
			Object[] arguments = (Object[]) args;
			while (arguments.length > 0 && arguments[0] instanceof String
					&& ((String) arguments[0]).startsWith("-")) {
				System.arraycopy(arguments, 1,
						arguments = new Object[arguments.length - 1], 0,
						arguments.length);
			}
			if (arguments.length == 4) {
				if (arguments[0] instanceof String
						&& arguments[1] instanceof String
						&& arguments[2] instanceof String
						&& arguments[3] instanceof String) {
					userName = (String) arguments[0];
					String hostName = (String) arguments[1];
					String password = (String) arguments[2];
					String roomName = (String) arguments[3];
					runRobot(hostName, password, roomName);
					return new Integer(0);
				}
			}
		}
		System.out
				.println("Usage: pass in four arguments (username, hostname, password, roomname)");
		return new Integer(-1);
	}

	private void runRobot(String hostName, String password, String roomName)
			throws ECFException, Exception, InterruptedException {
		XMPPChatClient client = new XMPPChatClient(this);
		client.connect(userName + "@" + hostName, password);

		IChatRoomContainer room = client.createChatRoom(roomName);
		room.connect(client.getChatRoomInfo().getRoomID(), null);

		System.out.println(room.getConnectedID().getName());
		room.addMessageListener(this);
		sender = room.getChatRoomMessageSender();
		running = true;
		sender
				.sendMessage("Hi, I'm a robot. To get rid of me, send me a direct message.");
		while (running) {
			wait();
		}
	}

	public synchronized void handleMessage(String from, String msg) {
		// direct message
		try {
			sender.sendMessage("gotta run");
		} catch (ECFException e) {
			e.printStackTrace();
		}
		running = false;
		notifyAll();
	}

	public void handleMessage(ID fromID, String messageBody) {
		// message in chat room
		if (fromID.getName().startsWith(userName + "@")) {
			// my own message, don't respond
			return;
		}
		try {
			if (messageBody.indexOf("e") != -1) {
				sender.sendMessage("kewl");
			} else if (messageBody.indexOf("s") != -1) {
				sender.sendMessage(";-)");
			} else {
				sender.sendMessage("'s up?");
			}
		} catch (ECFException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.presence.IIMMessageListener#handleMessageEvent(org.eclipse.ecf.presence.IIMMessageEvent)
	 */
	public void handleMessageEvent(IIMMessageEvent messageEvent) {
		if (messageEvent instanceof IChatRoomMessageEvent) {
			IChatRoomMessage m = ((IChatRoomMessageEvent) messageEvent)
					.getChatRoomMessage();
			handleMessage(m.getFromID(), m.getMessage());
		}
	}

}
