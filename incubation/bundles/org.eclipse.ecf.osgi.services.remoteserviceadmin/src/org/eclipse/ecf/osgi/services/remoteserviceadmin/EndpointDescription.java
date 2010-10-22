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

	public EndpointDescription(ServiceReference reference, Map osgiProperties) {
		super(reference, osgiProperties);
		initRemoteServiceProperties();
		computeHashCode();
	}

	private void computeHashCode() {
		this.hashCode = super.hashCode();
		this.hashCode = 31 * hashCode + containerID.hashCode();
		this.hashCode = 31 * hashCode
				+ (int) (remoteServiceId ^ (remoteServiceId >>> 32));
	}

	private void initRemoteServiceProperties() {
		Map properties = getProperties();

		containerID = (ID) properties.get(IConstants.CONTAINER_ID_PROPNAME);
		if (containerID == null)
			throw new NullPointerException(
					"ECF EndpointDescription must include non-null containerID");

		Object rsid = properties.get(IConstants.REMOTE_SERVICE_ID_PROPNAME);
		if (rsid != null)
			remoteServiceId = ((Long) rsid).longValue();

		Object ctid = properties.get(IConstants.CONNECT_TARGET_ID_PROPNAME);
		if (ctid != null)
			connectTargetID = (ID) ctid;

		Object idf = properties.get(IConstants.IDFILTER_PROPNAME);
		if (idf != null)
			idFilter = (ID[]) idf;

		Object rsf = properties.get(IConstants.REMOTESERVICE_FILTER_PROPNAME);
		if (rsf != null)
			rsFilter = (String) rsFilter;
	}

	public EndpointDescription(Map osgiProperties) {
		super(osgiProperties);
		initRemoteServiceProperties();
		computeHashCode();
	}

	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (!(other instanceof EndpointDescription))
			return false;
		EndpointDescription o = (EndpointDescription) other;
		return super.equals(other) && (o.containerID.equals(this.containerID))
				&& (o.remoteServiceId == this.remoteServiceId);
	}

	public int hashCode() {
		return hashCode;
	}

	public ID getID() {
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
