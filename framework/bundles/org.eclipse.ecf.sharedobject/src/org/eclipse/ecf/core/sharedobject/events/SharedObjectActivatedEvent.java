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

public class SharedObjectActivatedEvent implements ISharedObjectActivatedEvent {
	private ID activatedID;

	private ID localContainerID;

	/**
	 * @since 2.6
	 */
	public SharedObjectActivatedEvent() {

	}

	public SharedObjectActivatedEvent(ID container, ID act) {
		super();
		this.localContainerID = container;
		this.activatedID = act;
	}

	public ID getActivatedID() {
		return activatedID;
	}

	public ID getLocalContainerID() {
		return localContainerID;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("SharedObjectActivatedEvent["); //$NON-NLS-1$
		sb.append(getLocalContainerID()).append(";"); //$NON-NLS-1$
		sb.append(getActivatedID()).append("]"); //$NON-NLS-1$
		return sb.toString();
	}
}