/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.xmpp.smack;

import org.eclipse.ecf.core.comm.AsynchConnectionEvent;
import org.eclipse.ecf.core.comm.IAsynchConnection;
import org.jivesoftware.smack.packet.Packet;

public class ChatConnectionObjectPacketEvent extends AsynchConnectionEvent {

    Object value;
	/**
	 * @param source
	 * @param param
	 */
	public ChatConnectionObjectPacketEvent(IAsynchConnection source, Packet p, Object obj) {
		super(source,p);
		this.value = obj;
	}
	
	public Object getObjectValue() {
	    return value;
	}
	public String toString() {
	    StringBuffer sb = new StringBuffer("ChatConnectionPacketEvent[");
	    sb.append(getData()).append(";");
	    sb.append(getConnection()).append(";");
	    sb.append(getObjectValue()).append("]");
	    return sb.toString();
	}
}
