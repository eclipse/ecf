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

package org.eclipse.ecf.discovery;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;

/**
 * Base event implementation of {@link IServiceEvent}. Subclasses may be created
 * as appropriate.
 */
public class ServiceTypeContainerEvent implements IServiceTypeEvent {

	protected IServiceTypeID serviceType;

	protected ID containerID;

	public ServiceTypeContainerEvent(IServiceTypeID serviceType, ID containerID) {
		this.serviceType = serviceType;
		this.containerID = containerID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.events.IContainerEvent#getLocalContainerID()
	 */
	public ID getLocalContainerID() {
		return containerID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		final StringBuffer buf = new StringBuffer("ServiceTypeContainerEvent["); //$NON-NLS-1$
		buf.append("servicetypeid=").append(getServiceTypeID()).append(";containerid=").append(getLocalContainerID()).append("]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return buf.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.discovery.IServiceTypeEvent#getServiceTypeID()
	 */
	public IServiceTypeID getServiceTypeID() {
		return serviceType;
	}
}
