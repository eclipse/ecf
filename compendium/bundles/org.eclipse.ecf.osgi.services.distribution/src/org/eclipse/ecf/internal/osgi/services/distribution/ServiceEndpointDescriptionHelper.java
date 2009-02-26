/*******************************************************************************
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
******************************************************************************/
/**
 * 
 */
package org.eclipse.ecf.internal.osgi.services.distribution;

import java.util.Collection;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.osgi.services.discovery.ServiceEndpointDescriptionImpl;
import org.eclipse.ecf.remoteservice.Constants;

class ServiceEndpointDescriptionHelper {
	private static final long DEFAULT_FUTURE_TIMEOUT = 30000;

	private final ServiceEndpointDescriptionImpl description;
	private final ID discoveryContainerID;
	private final IServiceID serviceID;
	private final String serviceName;

	public ServiceEndpointDescriptionHelper(ServiceEndpointDescriptionImpl d)
			throws NullPointerException {
		description = d;
		// Get ECF discovery container ID...if not found there is a problem
		discoveryContainerID = description.getDiscoveryContainerID();
		if (discoveryContainerID == null)
			throw new NullPointerException(
					"ServiceEndpointDescription discoveryContainerID cannot be null");
		// Get serviceName from description
		serviceID = description.getServiceID();
		if (serviceID == null)
			throw new NullPointerException(
					"ServiceEndpointDescription serviceID cannot be null");
		serviceName = serviceID.getName();
		if (serviceName == null)
			throw new NullPointerException(
					"ServiceEndpointDescription serviceName is null");
	}

	public ServiceEndpointDescriptionImpl getDescription() {
		return description;
	}

	public ID getDiscoveryContainerID() {
		return discoveryContainerID;
	}

	public IServiceID getServiceID() {
		return serviceID;
	}

	public String getServiceName() {
		return serviceName;
	}

	public Collection getProvidedInterfaces() {
		Collection c = description.getProvidedInterfaces();
		if (c == null)
			throw new NullPointerException(
					"ServiceEndpointDescription providedInterfaces cannot be null");
		return c;
	}

	public ID getEndpointID() throws IDCreateException {
		String endpointID = description.getEndpointID();
		if (endpointID == null)
			throw new IDCreateException(
					"ServiceEndpointDescription endpointID cannot be null");
		// Get idfilter namespace name
		String idfilterNamespaceName = (String) description
				.getProperty(Constants.SERVICE_IDFILTER_NAMESPACE);
		if (idfilterNamespaceName == null)
			throw new IDCreateException(
					"IDfilter Namespace name is not set in description "
							+ description);
		return IDFactory.getDefault().createID(idfilterNamespaceName,
				endpointID);
	}

	public long getFutureTimeout() {
		return DEFAULT_FUTURE_TIMEOUT;
	}

}