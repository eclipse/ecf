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

import org.eclipse.ecf.core.identity.ID;

public class RemoteSharedObjectCreateResponseEvent extends RemoteSharedObjectEvent implements ISharedObjectCreateResponseEvent {
	private static final long serialVersionUID = 3618421544527738673L;

	long sequence = 0;

	/**
	 * @since 2.6
	 */
	public RemoteSharedObjectCreateResponseEvent() {

	}

	public RemoteSharedObjectCreateResponseEvent(ID senderObj, ID remoteCont, long seq, Throwable exception) {
		super(senderObj, remoteCont, exception);
		this.sequence = seq;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.events.ISharedObjectCreateResponseEvent#getSequence()
	 */
	public long getSequence() {
		return sequence;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.events.ISharedObjectCreateResponseEvent#getException()
	 */
	public Throwable getException() {
		return (Throwable) getData();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("RemoteSharedObjectCreateResponseEvent["); //$NON-NLS-1$
		sb.append(getSenderSharedObjectID()).append(";"); //$NON-NLS-1$
		sb.append(getRemoteContainerID()).append(";"); //$NON-NLS-1$
		sb.append(getSequence()).append(";"); //$NON-NLS-1$
		sb.append(getException()).append("]"); //$NON-NLS-1$
		return sb.toString();
	}
}