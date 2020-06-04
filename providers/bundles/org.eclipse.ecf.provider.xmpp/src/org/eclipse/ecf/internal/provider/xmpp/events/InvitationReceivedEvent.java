/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.xmpp.events;

import org.eclipse.ecf.core.util.Event;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;

public class InvitationReceivedEvent implements Event {
	XMPPConnection connection;
	String room;
	String inviter;
	String reason;
	String password;
	Message message;

	public InvitationReceivedEvent(XMPPConnection conn, String room,
			String inviter, String reason, String password, Message message) {
		super();
		this.connection = conn;
		this.room = room;
		this.inviter = inviter;
		this.reason = reason;
		this.password = password;
		this.message = message;
	}

	public XMPPConnection getConnection() {
		return connection;
	}

	public String getInviter() {
		return inviter;
	}

	public Message getMessage() {
		return message;
	}

	public String getPassword() {
		return password;
	}

	public String getReason() {
		return reason;
	}

	public String getRoom() {
		return room;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("InvitationReceivedEvent[");
		buf.append("conn=" + getConnection()).append(";room=" + getRoom());
		buf.append(";inviter=" + getInviter()).append(";reason=" + reason);
		buf.append(";pw=" + password).append(";msg=" + getMessage())
				.append("]");
		return buf.toString();
	}
}
