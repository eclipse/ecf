/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.sharedobject.events;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.SharedObjectDescription;

/**
 * @author slewis
 * 
 */
public class SharedObjectManagerCreateEvent implements
		ISharedObjectManagerEvent {
	private static final long serialVersionUID = 3905527103070878006L;
	SharedObjectDescription description = null;
	ID localContainerID = null;

	public SharedObjectManagerCreateEvent(ID localContainerID,
			SharedObjectDescription description) {
		this.localContainerID = localContainerID;
		this.description = description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.events.IContainerEvent#getLocalContainerID()
	 */
	public ID getLocalContainerID() {
		return localContainerID;
	}

	public SharedObjectDescription getDescription() {
		return description;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("SharedObjectManagerCreateEvent[");
		buf.append(getLocalContainerID()).append(";");
		buf.append(getDescription()).append("]");
		return buf.toString();
	}
}
