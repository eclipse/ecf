/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 * Boris Bokowski, IBM
 ******************************************************************************/
package org.eclipse.ecf.example.clients.applications;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.example.clients.IMessageReceiver;
import org.eclipse.ecf.example.clients.XMPPChatRoomClient;
import org.eclipse.ecf.presence.IIMMessageEvent;
import org.eclipse.ecf.presence.IIMMessageListener;
import org.eclipse.ecf.presence.chatroom.IChatRoomContainer;
import org.eclipse.ecf.presence.chatroom.IChatRoomMessage;
import org.eclipse.ecf.presence.chatroom.IChatRoomMessageEvent;
import org.eclipse.ecf.presence.chatroom.IChatRoomMessageSender;
import org.eclipse.ecf.presence.im.IChatMessage;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

/**
 * To be started as an application. Go to Run->Run..., create a new Eclipse
 * Application, select org.eclipse.ecf.example.clients.robot as the application
 * and make sure you have all required plug-ins.
 * 
 */
public class ChatRoomRobotApplication implements IApplication,
		IMessageReceiver, IIMMessageListener {

	private IChatRoomMessageSender sender;

	private boolean running = false;

	private String userName;

	public Object start(IApplicationContext context) throws Exception {
		Object[] args = context.getArguments().values().toArray();
		while (args[0] instanceof Object[])
			args = (Object[]) args[0];
		Object[] arguments = (Object[]) args;
		int l = arguments.length;
		if (arguments[l - 1] instanceof String
				&& arguments[l - 2] instanceof String
				&& arguments[l - 3] instanceof String
				&& arguments[l - 4] instanceof String) {
			userName = (String) arguments[l - 4];
			String hostName = (String) arguments[l - 3];
			String password = (String) arguments[l - 2];
			String roomName = (String) arguments[l - 1];
			runRobot(hostName, password, roomName);
			return new Integer(0);
		}
		System.out
				.println("Usage: pass in four arguments (username, hostname, password, roomname)");
		return new Integer(-1);
	}

	public void stop() {
	}

	private void runRobot(String hostName, String password, String roomName)
			throws ECFException, Exception, InterruptedException {
		XMPPChatRoomClient client = new XMPPChatRoomClient(this);

		// Then connect
		String connectTarget = userName + "@" + hostName;

		client.connect(connectTarget, password);

		IChatRoomContainer room = client.createChatRoom(roomName);
		room.connect(client.getChatRoomInfo().getRoomID(), null);

		System.out.println("ECF chat room robot (" + connectTarget
				+ ").  Connected to room: "
				+ client.getChatRoomInfo().getRoomID().getName());

		room.addMessageListener(this);
		sender = room.getChatRoomMessageSender();
		running = true;
		sender
				.sendMessage("Hi, I'm a robot. To get rid of me, send me a direct message.");
		while (running) {
			wait();
		}
	}

	public synchronized void handleMessage(IChatMessage chatMessage) {
		// direct message
		try {
			sender.sendMessage("gotta run");
		} catch (ECFException e) {
			e.printStackTrace();
		}
		running = false;
		notifyAll();
	}

	public void handleChatRoomMessage(ID fromID, String messageBody) {
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

	public void handleMessageEvent(IIMMessageEvent messageEvent) {
		if (messageEvent instanceof IChatRoomMessageEvent) {
			IChatRoomMessage m = ((IChatRoomMessageEvent) messageEvent)
					.getChatRoomMessage();
			handleChatRoomMessage(m.getFromID(), m.getMessage());
		}
	}

}
