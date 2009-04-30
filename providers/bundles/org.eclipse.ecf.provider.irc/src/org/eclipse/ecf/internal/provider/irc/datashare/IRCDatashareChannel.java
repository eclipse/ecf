/******************************************************************************
 * Copyright (c) 2009 Remy Chi Jian Suen and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Remy Chi Jian Suen - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.provider.irc.datashare;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.datashare.IChannelListener;
import org.eclipse.ecf.internal.provider.irc.Activator;
import org.eclipse.ecf.internal.provider.irc.identity.IRCID;
import org.eclipse.ecf.presence.chatroom.IChatRoomMessageSender;
import org.eclipse.ecf.provider.datashare.nio.NIOChannel;
import org.eclipse.ecf.provider.datashare.nio.NIODatashareContainer;

class IRCDatashareChannel extends NIOChannel {

	private String ip;

	private IChatRoomMessageSender sender;

	IRCDatashareChannel(NIODatashareContainer datashareContainer, ID userId,
			IChatRoomMessageSender sender, ID id, IChannelListener listener)
			throws ECFException {
		super(datashareContainer, userId, id, listener);
		this.sender = sender;
	}

	protected void log(IStatus status) {
		Activator.getDefault().log(status);
	}

	/**
	 * Sets the IP that should be sent to the remote peer for connecting with
	 * the local computer.
	 * 
	 * @param ip
	 *            the local computer's IP
	 */
	void setIP(String ip) {
		this.ip = ip;
	}

	protected void sendRequest(ID receiver) throws ECFException {
		// if we don't, we need to send a request and then connect
		IRCID id = (IRCID) receiver;

		StringBuffer buffer = new StringBuffer();
		buffer.append("/msg ").append(id.getUsername()); //$NON-NLS-1$
		buffer.append(" \01ECF "); //$NON-NLS-1$
		buffer.append(ip).append(':').append(getLocalPort());
		buffer.append('\01');

		sender.sendMessage(buffer.toString());
	}

	public void sendMessage(ID receiver, byte[] message) throws ECFException {
		Assert.isNotNull(receiver, "A receiver must be specified"); //$NON-NLS-1$
		Assert.isLegal(receiver instanceof IRCID,
				"Receiver id must be an IRCID"); //$NON-NLS-1$
		super.sendMessage(receiver, message);
	}

}
