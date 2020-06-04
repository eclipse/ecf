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

package org.eclipse.ecf.example.collab.share;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.events.RemoteSharedObjectEvent;

public class RemoteSharedObjectMsgEvent extends RemoteSharedObjectEvent {
	private static final long serialVersionUID = -7198080945310388254L;

	/**
	 * @param senderObj
	 * @param remoteCont
	 * @param msg
	 */
	public RemoteSharedObjectMsgEvent(ID senderObj, ID remoteCont,
			SharedObjectMsg msg) {
		super(senderObj, remoteCont, msg);
	}

	public SharedObjectMsg getMsg() {
		return (SharedObjectMsg) super.getData();
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("RemoteSharedObjectMsgEvent["); //$NON-NLS-1$
		buf.append(getSenderSharedObjectID()).append(";").append( //$NON-NLS-1$
				getRemoteContainerID()).append(";").append(getMsg()); //$NON-NLS-1$
		buf.append("]"); //$NON-NLS-1$
		return buf.toString();
	}
}
