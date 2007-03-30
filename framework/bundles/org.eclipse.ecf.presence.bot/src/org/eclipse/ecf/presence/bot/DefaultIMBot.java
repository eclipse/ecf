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
package org.eclipse.ecf.presence.bot;

import java.util.List;

import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.security.ConnectContextFactory;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.internal.presence.bot.Messages;
import org.eclipse.ecf.presence.IIMMessageEvent;
import org.eclipse.ecf.presence.IIMMessageListener;
import org.eclipse.ecf.presence.IPresenceContainerAdapter;
import org.eclipse.ecf.presence.im.IChatMessage;
import org.eclipse.ecf.presence.im.IChatMessageEvent;

public class DefaultIMBot implements IIMMessageListener {

	protected IIMBotEntry bot;
	protected IContainer container;
	protected ID targetID;

	public DefaultIMBot(IIMBotEntry bot) {
		this.bot = bot;
	}

	protected void fireInitBot() {
		List commands = bot.getCommands();
		for (int i = 0; i < commands.size(); i++) {
			IIMMessageHandlerEntry entry = (IIMMessageHandlerEntry) commands
					.get(i);
			entry.getHandler().initRobot(bot);
		}
	}

	protected void fireInit() {
		List commands = bot.getCommands();
		for (int i = 0; i < commands.size(); i++) {
			IIMMessageHandlerEntry entry = (IIMMessageHandlerEntry) commands
					.get(i);
			entry.getHandler().init(container);
		}
	}

	protected void firePreConnect() {
		List commands = bot.getCommands();
		for (int i = 0; i < commands.size(); i++) {
			IIMMessageHandlerEntry entry = (IIMMessageHandlerEntry) commands
					.get(i);
			entry.getHandler().preContainerConnect(targetID);
		}
	}

	public synchronized void connect() throws ECFException {
		
		fireInitBot();
		
		try {
			Namespace namespace = null;

			if (container == null) {
				container = ContainerFactory.getDefault().createContainer(
						bot.getContainerFactoryName());
				namespace = container.getConnectNamespace();
			} else
				throw new ContainerConnectException(
						Messages.DefaultChatRoomBot_EXCEPTION_ALREADY_CONNECTED);

			fireInit();

			targetID = IDFactory.getDefault().createID(namespace,
					bot.getConnectID());

			firePreConnect();

			IPresenceContainerAdapter presenceAdapter = (IPresenceContainerAdapter) container
					.getAdapter(IPresenceContainerAdapter.class);

			presenceAdapter.getChatManager().addMessageListener(this);

			String password = bot.getPassword();
			IConnectContext context = (password == null) ? null
					: ConnectContextFactory
							.createPasswordConnectContext(password);

			container.connect(targetID, context);

		} catch (ECFException e) {
			if (container != null) {
				if (container.getConnectedID() != null) {
					container.disconnect();
				}
				container.dispose();
			}
			container = null;
			throw e;
		}

	}

	public void handleMessageEvent(IIMMessageEvent event) {
		if (event instanceof IChatMessageEvent) {
			IChatMessageEvent imEvent = (IChatMessageEvent) event;
			IChatMessage message = imEvent.getChatMessage();
			List commands = bot.getCommands();
			for (int i = 0; i < commands.size(); i++) {
				IIMMessageHandlerEntry entry = (IIMMessageHandlerEntry) commands
						.get(i);
				entry.handleIMMessage(message);
			}
		}
	}

}
