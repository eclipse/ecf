/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.sharedobject.events;

import java.util.Map;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.ISharedObject;

/**
 * @author slewis
 * 
 */
public class SharedObjectManagerAddEvent implements ISharedObjectManagerEvent {
	private static final long serialVersionUID = 3258413923916330551L;
	ID localContainerID = null;
	Map properties = null;
	ISharedObject sharedObject = null;
	ID sharedObjectID = null;

	public SharedObjectManagerAddEvent(ID localContainerID, ID sharedObjectID,
			ISharedObject object, Map properties) {
		this.localContainerID = localContainerID;
		this.sharedObjectID = sharedObjectID;
		this.sharedObject = object;
		this.properties = properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.events.IContainerEvent#getLocalContainerID()
	 */
	public ID getLocalContainerID() {
		return localContainerID;
	}

	public Map getProperties() {
		return properties;
	}

	public ISharedObject getSharedObject() {
		return sharedObject;
	}

	public ID getSharedObjectID() {
		return sharedObjectID;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("SharedObjectManagerAddEvent[");
		buf.append(getLocalContainerID()).append(";");
		buf.append(getSharedObjectID()).append(";");
		buf.append(getSharedObject()).append(";");
		buf.append(getProperties()).append("]");
		return buf.toString();
	}
}
