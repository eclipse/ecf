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

import org.eclipse.ecf.core.util.Event;

public class ChatMembershipEvent implements Event {

	String id;
	boolean add;

	public ChatMembershipEvent(String id, boolean add) {
		this.id = id;
		this.add = add;
	}

	public String getFrom() {
		return id;
	}

	public boolean isAdd() {
		return add;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("ChatMembershipEvent[");
		buf.append(id).append(";").append(add).append("]");
		return buf.toString();
	}
}
