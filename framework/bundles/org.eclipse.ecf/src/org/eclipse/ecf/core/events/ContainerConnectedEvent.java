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

import org.eclipse.ecf.core.identity.ID;

/**
 * Container connected event
 */
public class ContainerConnectedEvent implements IContainerConnectedEvent {
	private final ID joinedContainerID;

	private final ID localContainerID;

	public ContainerConnectedEvent(ID localContainerID, ID targetID) {
		super();
		this.localContainerID = localContainerID;
		this.joinedContainerID = targetID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.events.IContainerConnectedEvent#getTargetID()
	 */
	public ID getTargetID() {
		return joinedContainerID;
	}

	public ID getLocalContainerID() {
		return localContainerID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer("ContainerConnectedEvent["); //$NON-NLS-1$
		buf.append(getLocalContainerID()).append("]"); //$NON-NLS-1$
		buf.append(getTargetID()).append(";"); //$NON-NLS-1$
		return buf.toString();
	}
}