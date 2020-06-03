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
package org.eclipse.ecf.core.events;

import java.io.Serializable;
import org.eclipse.ecf.core.identity.ID;

public class ContainerEjectedEvent implements IContainerEjectedEvent {
	private final ID localContainerID;

	private final ID groupID;

	private final Serializable reason;

	public ContainerEjectedEvent(ID localContainerID, ID targetID, Serializable reason) {
		super();
		this.localContainerID = localContainerID;
		this.groupID = targetID;
		this.reason = reason;
	}

	public ID getTargetID() {
		return groupID;
	}

	public ID getLocalContainerID() {
		return localContainerID;
	}

	public Serializable getReason() {
		return reason;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer("ContainerEjectedEvent["); //$NON-NLS-1$
		buf.append(getLocalContainerID()).append(";"); //$NON-NLS-1$
		buf.append(getTargetID()).append(";"); //$NON-NLS-1$
		buf.append(getReason()).append("]"); //$NON-NLS-1$
		return buf.toString();
	}
}