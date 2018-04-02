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

package org.eclipse.ecf.core.sharedobject;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.events.RemoteSharedObjectEvent;

/**
 * Event type to represent messages sent between shared objects
 * 
 */
public class SharedObjectMsgEvent extends RemoteSharedObjectEvent {

	private static final long serialVersionUID = -8674874265514762123L;

	public SharedObjectMsgEvent() {
		super();
	}

	/**
	 * @param senderObj
	 * @param remoteCont
	 * @param msg
	 */
	public SharedObjectMsgEvent(ID senderObj, ID remoteCont, SharedObjectMsg msg) {
		super(senderObj, remoteCont, msg);
	}

	public SharedObjectMsg getSharedObjectMsg() {
		return (SharedObjectMsg) super.getData();
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("SharedObjectMsgEvent["); //$NON-NLS-1$
		buf.append(getSenderSharedObjectID()).append(";").append( //$NON-NLS-1$
				getRemoteContainerID()).append(";") //$NON-NLS-1$
				.append(getSharedObjectMsg());
		buf.append("]"); //$NON-NLS-1$
		return buf.toString();
	}
}
