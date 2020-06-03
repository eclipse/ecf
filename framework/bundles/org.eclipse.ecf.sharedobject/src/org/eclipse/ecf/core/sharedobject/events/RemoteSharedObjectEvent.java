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
package org.eclipse.ecf.core.sharedobject.events;

import java.io.Serializable;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.Event;

public class RemoteSharedObjectEvent implements ISharedObjectMessageEvent, Serializable {
	private static final long serialVersionUID = 3257572797621680182L;

	private ID senderSharedObjectID;

	private ID remoteContainerID;

	private Object data;

	/**
	 * @since 2.6
	 */
	public RemoteSharedObjectEvent() {
	}

	public RemoteSharedObjectEvent(ID senderObj, ID remoteCont, Object data) {
		super();
		this.senderSharedObjectID = senderObj;
		this.remoteContainerID = remoteCont;
		this.data = data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.api.events.ISharedObjectEvent#getSenderSharedObject()
	 */
	public ID getSenderSharedObjectID() {
		return senderSharedObjectID;
	}

	public ID getRemoteContainerID() {
		return remoteContainerID;
	}

	public Event getEvent() {
		return this;
	}

	public Object getData() {
		return data;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("RemoteSharedObjectEvent["); //$NON-NLS-1$
		sb.append(getSenderSharedObjectID()).append(";"); //$NON-NLS-1$
		sb.append(getRemoteContainerID()).append(";"); //$NON-NLS-1$
		sb.append(getData()).append("]"); //$NON-NLS-1$
		return sb.toString();
	}
}