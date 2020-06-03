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
import org.eclipse.ecf.core.sharedobject.ISharedObjectConnector;
import org.eclipse.ecf.core.sharedobject.ISharedObjectManager;

/**
 * Shared object manager connection event. Instances implementing this interface
 * are sent to IContainerListeners when the
 * {@link ISharedObjectManager#disconnectSharedObjects(ISharedObjectConnector)}
 * is called.
 * 
 */
public class SharedObjectManagerDisconnectEvent implements ISharedObjectManagerConnectionEvent {
	ID localContainerID = null;

	ISharedObjectConnector connector = null;

	/**
	 * @since 2.6
	 */
	public SharedObjectManagerDisconnectEvent() {

	}

	public SharedObjectManagerDisconnectEvent(ID localContainerID, ISharedObjectConnector connector) {
		this.localContainerID = localContainerID;
		this.connector = connector;
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
		return connector.getSenderID();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.sharedobject.events.ISharedObjectManagerConnectionEvent#getConnector()
	 */
	public ISharedObjectConnector getConnector() {
		return connector;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("SharedObjectManagerDisconnectEvent["); //$NON-NLS-1$
		buf.append(getLocalContainerID()).append(";"); //$NON-NLS-1$
		buf.append(getSharedObjectID()).append(";"); //$NON-NLS-1$
		buf.append(getConnector()).append("]"); //$NON-NLS-1$
		return buf.toString();
	}
}
