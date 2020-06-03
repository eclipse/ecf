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

package org.eclipse.ecf.core.sharedobject;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.events.RemoteSharedObjectEvent;

/**
 * Event type to represent messages sent between shared objects
 * 
 */
public class SharedObjectMsgEvent extends RemoteSharedObjectEvent {

	private static final long serialVersionUID = -8674874265514762123L;

	/**
	 * @since 2.6
	 */
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
