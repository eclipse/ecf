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

public class ContainerDisconnectedEvent implements IContainerDisconnectedEvent {
	private static final long serialVersionUID = 3256437002059527733L;

	private final ID departedContainerID;

	private final ID localContainerID;

	public ContainerDisconnectedEvent(ID container, ID o) {
		this.localContainerID = container;
		this.departedContainerID = o;
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
		buf.append(getTargetID()).append(";"); //$NON-NLS-1$
		buf.append(getLocalContainerID()).append(";").append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		return buf.toString();
	}
}