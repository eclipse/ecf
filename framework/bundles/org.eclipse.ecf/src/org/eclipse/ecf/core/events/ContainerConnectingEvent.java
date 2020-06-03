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

public class ContainerConnectingEvent implements IContainerConnectingEvent {
	ID localContainerID;

	ID targetID;

	Object data;

	public ContainerConnectingEvent(ID localContainerID, ID targetID, Object data) {
		this.localContainerID = localContainerID;
		this.targetID = targetID;
		this.data = data;
	}

	public ContainerConnectingEvent(ID localContainerID, ID targetID) {
		this(localContainerID, targetID, null);
	}

	public ID getTargetID() {
		return targetID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.events.IContainerConnectingEvent#getData()
	 */
	public Object getData() {
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.events.IContainerEvent#getLocalContainerID()
	 */
	public ID getLocalContainerID() {
		return localContainerID;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("ContainerConnectingEvent["); //$NON-NLS-1$
		buf.append(getLocalContainerID()).append(";"); //$NON-NLS-1$
		buf.append(getTargetID()).append(";"); //$NON-NLS-1$
		buf.append(getData()).append("]"); //$NON-NLS-1$
		return buf.toString();
	}
}