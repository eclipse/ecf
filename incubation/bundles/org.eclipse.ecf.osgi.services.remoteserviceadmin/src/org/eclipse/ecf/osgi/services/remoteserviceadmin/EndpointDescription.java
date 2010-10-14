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

import java.net.URI;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.IServiceProperties;
import org.eclipse.ecf.discovery.ServiceInfo;
import org.eclipse.ecf.discovery.ServiceProperties;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.osgi.framework.ServiceReference;

public class EndpointDescription extends
		org.osgi.service.remoteserviceadmin.EndpointDescription {

	private ID containerID;
	private long remoteServiceId;
	private ID targetID;
	private ID[] idFilter;

	public EndpointDescription(ServiceReference reference, Map osgiProperties,
			ID containerID, long remoteServiceId, ID targetID, ID[] idFilter) {
		super(reference, osgiProperties);
		this.containerID = containerID;
		Assert.isNotNull(this.containerID);
		this.remoteServiceId = remoteServiceId;
		this.targetID = targetID;
		this.idFilter = idFilter;
	}

	public EndpointDescription(IServiceInfo discoveredServiceInfo,
			Map osgiProperties) {
		super(osgiProperties);
		// XXX todo...add to get ECF remote services-specific things from the
		// received IServiceInfo.

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
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	public ID getID() {
		return containerID;
	}

	public ID getTargetID() {
		return targetID;
	}

	public long getRemoteServiceId() {
		return remoteServiceId;
	}

	public ID[] getIDFilter() {
		return idFilter;
	}

	private IServiceProperties getServiceInfoServiceProperties() {
		IServiceProperties result = new ServiceProperties();
		// TODO...fill out IServiceProperties from EndpointDescription properties and ECF fields
		return result;
	}
	
	public IServiceInfo createServiceInfo(URI location, String serviceName, IServiceTypeID serviceTypeID, int priority, int weight, long ttl) {
		return new ServiceInfo(location, serviceName, serviceTypeID, priority, weight, getServiceInfoServiceProperties(), ttl);
	}
}
