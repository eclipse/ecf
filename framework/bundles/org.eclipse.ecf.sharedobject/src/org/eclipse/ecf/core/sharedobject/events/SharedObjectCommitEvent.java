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

public class SharedObjectCommitEvent implements ISharedObjectCommitEvent, Serializable {
	private static final long serialVersionUID = 4615634472917480497L;

	ID senderSharedObjectID = null;

	Event event = null;

	/**
	 * @since 2.6
	 */
	public SharedObjectCommitEvent() {

	}

	public SharedObjectCommitEvent(ID senderSharedObjectID, Event event) {
		super();
		this.senderSharedObjectID = senderSharedObjectID;
		this.event = event;
	}

	public SharedObjectCommitEvent(ID senderSharedObjectID) {
		this(senderSharedObjectID, null);
	}

	public ID getSenderSharedObjectID() {
		return senderSharedObjectID;
	}

	public Event getEvent() {
		return event;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("SharedObjectCommitEvent["); //$NON-NLS-1$
		sb.append(getSenderSharedObjectID()).append(";"); //$NON-NLS-1$
		sb.append(getEvent()).append("]"); //$NON-NLS-1$
		return sb.toString();
	}
}
