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

public class SharedObjectDeactivatedEvent implements ISharedObjectDeactivatedEvent {
	private ID deactivatedID;

	private ID localContainerID;

	/**
	 * @since 2.6
	 */
	public SharedObjectDeactivatedEvent() {

	}

	public SharedObjectDeactivatedEvent(ID container, ID deact) {
		super();
		this.localContainerID = container;
		this.deactivatedID = deact;
	}

	public ID getDeactivatedID() {
		return deactivatedID;
	}

	public ID getLocalContainerID() {
		return localContainerID;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("SharedObjectDeactivatedEvent["); //$NON-NLS-1$
		sb.append(getLocalContainerID()).append(";"); //$NON-NLS-1$
		sb.append(getDeactivatedID()).append("]"); //$NON-NLS-1$
		return sb.toString();
	}
}