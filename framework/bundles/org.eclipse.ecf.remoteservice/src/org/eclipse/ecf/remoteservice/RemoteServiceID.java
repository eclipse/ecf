/****************************************************************************
 * Copyright (c) 2009 EclipseSource and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteservice;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.identity.*;

/**
 * @since 3.0
 */
public class RemoteServiceID extends BaseID implements IRemoteServiceID {

	private static final String SEPARATOR = "/"; //$NON-NLS-1$
	private static final long serialVersionUID = 8330802303633588267L;
	private ID containerID;
	private long containerRelative;
	private int hash;

	public RemoteServiceID(Namespace namespace, ID containerID, long containerRelative) {
		super(namespace);
		Assert.isNotNull(containerID);
		this.containerID = containerID;
		this.containerRelative = containerRelative;
		this.hash = 7;
		this.hash = 31 * hash + containerID.hashCode();
		this.hash = 31 * hash + (int) (containerRelative ^ (containerRelative >>> 32));
	}

	@SuppressWarnings("unchecked")
	protected int namespaceCompareTo(BaseID o) {
		if (o == null || !(o instanceof RemoteServiceID))
			return Integer.MIN_VALUE;
		RemoteServiceID other = (RemoteServiceID) o;
		int containerIDCompareResult = this.containerID.compareTo(other.containerID);
		if (containerIDCompareResult == 0)
			return (int) (this.containerRelative - other.containerRelative);
		return containerIDCompareResult;
	}

	protected boolean namespaceEquals(BaseID o) {
		if (this == o)
			return true;
		if (o == null || !(o instanceof RemoteServiceID))
			return false;
		RemoteServiceID other = (RemoteServiceID) o;
		if (this.containerID.equals(other.containerID))
			return this.containerRelative == other.containerRelative;
		return false;
	}

	protected String namespaceGetName() {
		return this.containerID.getName() + SEPARATOR + this.containerRelative;
	}

	protected int namespaceHashCode() {
		return hash;
	}

	public ID getContainerID() {
		return containerID;
	}

	public long getContainerRelativeID() {
		return containerRelative;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("org.eclipse.ecf.remoteservice.RemoteServiceID["); //$NON-NLS-1$
		buf.append("containerID=").append(getContainerID()); //$NON-NLS-1$
		buf.append(";containerRelativeID=").append(getContainerRelativeID()).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		return buf.toString();
	}
}
