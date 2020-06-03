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

/**
 * Base event implementation of {@link IServiceEvent}. Subclasses may be created
 * as appropriate.
 */
public class ServiceContainerEvent implements IServiceEvent {

	protected IServiceInfo info;

	protected ID containerID;

	public ServiceContainerEvent(IServiceInfo info, ID containerID) {
		this.info = info;
		this.containerID = containerID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.discovery.IServiceEvent#getServiceInfo()
	 */
	public IServiceInfo getServiceInfo() {
		return info;
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
		final StringBuffer buf = new StringBuffer("ServiceContainerEvent["); //$NON-NLS-1$
		buf.append("serviceinfo=").append(info).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		return buf.toString();
	}
}
