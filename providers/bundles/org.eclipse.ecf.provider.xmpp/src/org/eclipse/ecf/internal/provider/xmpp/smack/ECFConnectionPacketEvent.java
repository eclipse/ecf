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
package org.eclipse.ecf.internal.provider.xmpp.smack;

import org.eclipse.ecf.provider.comm.AsynchEvent;
import org.eclipse.ecf.provider.comm.IAsynchConnection;
import org.jivesoftware.smack.packet.Packet;

public class ECFConnectionPacketEvent extends AsynchEvent {

	public ECFConnectionPacketEvent(IAsynchConnection source, Packet p) {
		super(source, p);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("ECFConnectionPacketEvent[");
		sb.append(getData()).append(";");
		sb.append(getConnection()).append("]");
		return sb.toString();
	}
}
