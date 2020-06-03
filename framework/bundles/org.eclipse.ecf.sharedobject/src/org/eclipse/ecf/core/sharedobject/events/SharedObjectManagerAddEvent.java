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
import org.eclipse.ecf.core.sharedobject.ISharedObjectManager;

/**
 * Shared object manager event sent/triggered when a shared object is added to
 * container via
 * {@link ISharedObjectManager#addSharedObject(ID, org.eclipse.ecf.core.sharedobject.ISharedObject, java.util.Map)}
 * is called
 */
public class SharedObjectManagerAddEvent implements ISharedObjectManagerEvent {
	ID localContainerID = null;

	ID sharedObjectID = null;

	/**
	 * @since 2.6
	 */
	public SharedObjectManagerAddEvent() {

	}

	public SharedObjectManagerAddEvent(ID localContainerID, ID sharedObjectID) {
		this.localContainerID = localContainerID;
		this.sharedObjectID = sharedObjectID;
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
	 * @see org.eclipse.ecf.core.sharedobject.events.ISharedObjectManagerEvent#getSharedObjectID()
	 */
	public ID getSharedObjectID() {
		return sharedObjectID;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("SharedObjectManagerAddEvent["); //$NON-NLS-1$
		buf.append(getLocalContainerID()).append(";"); //$NON-NLS-1$
		buf.append(getSharedObjectID()).append("]"); //$NON-NLS-1$
		return buf.toString();
	}
}
