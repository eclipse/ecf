/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.events;

import org.eclipse.ecf.core.identity.ID;

public class ContainerConnectingEvent implements IContainerConnectingEvent {
	private static final long serialVersionUID = 3544952173248263729L;

	ID localContainerID;

	ID targetID;

	Object data;

	public ContainerConnectingEvent(ID localContainerID, ID groupID, Object data) {
		this.localContainerID = localContainerID;
		this.targetID = groupID;
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
		StringBuffer buf = new StringBuffer("ContainerConnectingEvent[");
		buf.append(getLocalContainerID()).append(";");
		buf.append(getTargetID()).append(";");
		buf.append(getData()).append("]");
		return buf.toString();
	}
}