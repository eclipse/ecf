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

import java.util.Map;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.osgi.framework.ServiceReference;

public class EndpointDescription extends
		org.osgi.service.remoteserviceadmin.EndpointDescription implements
		IEndpointDescription {

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
		// XXX todo
	}

	public EndpointDescription(IServiceInfo discoveredServiceInfo,
			Map osgiProperties) {
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

}
