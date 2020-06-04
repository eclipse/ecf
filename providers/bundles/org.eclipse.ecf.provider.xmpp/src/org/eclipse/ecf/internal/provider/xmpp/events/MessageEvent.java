/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.xmpp.events;

import java.util.Iterator;
import org.eclipse.ecf.core.util.Event;
import org.jivesoftware.smack.packet.Message;

public class MessageEvent implements Event {

	protected Message message = null;

	protected Iterator xhtmlbodies = null;

	public MessageEvent(Message message) {
		this(message, null);
	}

	public MessageEvent(Message message, Iterator xhtmlbodies) {
		this.message = message;
		this.xhtmlbodies = xhtmlbodies;
	}

	public Message getMessage() {
		return message;
	}

	public Iterator getXHTMLBodies() {
		return xhtmlbodies;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("MessageEvent[");
		buf.append(message).append(";")
				.append((message == null) ? "" : message.toXML()).append("]");
		return buf.toString();
	}
}
