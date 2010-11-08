/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import java.util.Arrays;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.identity.ID;
import org.osgi.framework.ServiceReference;

public class EndpointDescription extends
		org.osgi.service.remoteserviceadmin.EndpointDescription {

	private ID containerID;
	private long remoteServiceId;
	private ID connectTargetID;
	private ID[] idFilter;
	private String rsFilter;

	private int hashCode;

	public EndpointDescription(ServiceReference reference, Map osgiProperties,
			ID containerID, long remoteServiceId, ID connectTargetID,
			ID[] idFilter, String rsFilter) {
		super(reference, osgiProperties);
		this.containerID = containerID;
		Assert.isNotNull(this.containerID);
		this.remoteServiceId = remoteServiceId;
		this.connectTargetID = connectTargetID;
		this.idFilter = idFilter;
		this.rsFilter = rsFilter;
		computeHashCode();
	}

	public EndpointDescription(Map osgiProperties, ID containerID,
			long remoteServiceId, ID connectTargetID, ID[] idFilter,
			String rsFilter) {
		super(osgiProperties);
		this.containerID = containerID;
		Assert.isNotNull(this.containerID);
		this.remoteServiceId = remoteServiceId;
		this.connectTargetID = connectTargetID;
		this.idFilter = idFilter;
		this.rsFilter = rsFilter;
		computeHashCode();
	}

	private void computeHashCode() {
		this.hashCode = super.hashCode();
		this.hashCode = 31 * hashCode + containerID.hashCode();
		this.hashCode = 31 * hashCode
				+ (int) (remoteServiceId ^ (remoteServiceId >>> 32));
	}

	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (!(other instanceof EndpointDescription))
			return false;
		EndpointDescription o = (EndpointDescription) other;
		return super.equals(other) && (o.containerID.equals(this.containerID))
				&& (o.remoteServiceId == this.remoteServiceId);
	}

	public int hashCode() {
		return hashCode;
	}

	public ID getContainerID() {
		return containerID;
	}

	public ID getTargetID() {
		return connectTargetID;
	}

	public long getRemoteServiceId() {
		return remoteServiceId;
	}

	public ID[] getIDFilter() {
		return idFilter;
	}

	public String getRemoteServiceFilter() {
		return rsFilter;
	}

	public String toString() {
		return "ECFEndpointDescription[properties=" + super.toString()
				+ ",containerID=" + containerID + ", remoteServiceId="
				+ remoteServiceId + ", connectTargetID=" + connectTargetID
				+ ", idFilter=" + Arrays.toString(idFilter) + ", rsFilter="
				+ rsFilter + ", hashCode=" + hashCode + "]";
	}

}
