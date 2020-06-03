/****************************************************************************
 * Copyright (c) 2004, 2007 Composent, Inc. and others.
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
package org.eclipse.ecf.core.events;

import org.eclipse.ecf.core.identity.ID;

/**
 * Container disconnected event.
 */
public class ContainerDisconnectedEvent implements IContainerDisconnectedEvent {
	private final ID departedContainerID;

	private final ID localContainerID;

	/**
	 * Creates a new ContainerDisconnectedEvent to indicate that the container
	 * has now completely disconnected from its target host.
	 * 
	 * @param localContainerID
	 *            the ID of the local container
	 * @param targetID
	 *            the ID of the target
	 */
	public ContainerDisconnectedEvent(ID localContainerID, ID targetID) {
		this.localContainerID = localContainerID;
		this.departedContainerID = targetID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.events.IContainerDisconnectedEvent#getTargetID()
	 */
	public ID getTargetID() {
		return departedContainerID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.events.IContainerEvent#getLocalContainerID()
	 */
	public ID getLocalContainerID() {
		return localContainerID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer("ContainerDisconnectedEvent["); //$NON-NLS-1$
		buf.append(getLocalContainerID()).append(";").append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append(getTargetID()).append(";"); //$NON-NLS-1$
		return buf.toString();
	}
}