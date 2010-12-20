/*******************************************************************************
 * Copyright (c) 2010-2011 Composent, Inc. and others. All rights reserved. This
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

	private long remoteServiceId;

	private String containerIDNamespace;
	private ID connectTargetID;
	private ID[] idFilter;
	private String rsFilter;

	private int hashCode;

	public EndpointDescription(ServiceReference reference, Map osgiProperties,
			String containerIDNamespace, ID connectTargetID, ID[] idFilter,
			String rsFilter) {
		super(reference, osgiProperties);
		this.remoteServiceId = verifyLongProperty(org.eclipse.ecf.remoteservice.Constants.SERVICE_ID);
		this.containerIDNamespace = containerIDNamespace;
		this.connectTargetID = connectTargetID;
		this.idFilter = idFilter;
		this.rsFilter = rsFilter;
		computeHashCode();
	}

	public EndpointDescription(Map osgiProperties, String containerIDNamespace,
			ID connectTargetID, ID[] idFilter, String rsFilter) {
		super(osgiProperties);
		this.remoteServiceId = verifyLongProperty(org.eclipse.ecf.remoteservice.Constants.SERVICE_ID);
		this.containerIDNamespace = containerIDNamespace;
		this.connectTargetID = connectTargetID;
		this.idFilter = idFilter;
		this.rsFilter = rsFilter;
		computeHashCode();
	}

	private long verifyLongProperty(String propName) {
		Object r = getProperties().get(propName);
		if (r == null) {
			return 0l;
		}
		try {
			return ((Long) r).longValue();
		} catch (ClassCastException e) {
			IllegalArgumentException iae = new IllegalArgumentException(
					"property value is not a Long: " + propName);
			iae.initCause(e);
			throw iae;
		}
	}

	private void computeHashCode() {
		this.hashCode = super.hashCode();
		long remoteServiceId = getRemoteServiceId();
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
		return super.equals(other)
				&& (o.getRemoteServiceId() == this.getRemoteServiceId());
	}

	public int hashCode() {
		return hashCode;
	}

	public String getContainerIDNamespace() {
		return containerIDNamespace;
	}

	public ID getConnectTargetID() {
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
				+ ",containerIDNamespace=" + containerIDNamespace
				+ ", remoteServiceId=" + getRemoteServiceId()
				+ ", connectTargetID=" + connectTargetID + ", idFilter="
				+ Arrays.toString(idFilter) + ", rsFilter=" + rsFilter
				+ ", hashCode=" + hashCode + "]";
	}

}
