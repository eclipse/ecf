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

import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.discovery.identity.IServiceID;

public class DiscoveredEndpointDescription {

	private Namespace discoveryLocatorNamespace;
	private IServiceID serviceID;
	private EndpointDescription endpointDescription;
	private int hashCode = 7;

	public DiscoveredEndpointDescription(Namespace discoveryLocatorNamespace, IServiceID serviceID,
			EndpointDescription endpointDescription) {
		this.discoveryLocatorNamespace = discoveryLocatorNamespace;
		this.serviceID = serviceID;
		this.endpointDescription = endpointDescription;
		this.hashCode = 31 * this.hashCode
				+ discoveryLocatorNamespace.getName().hashCode();
		this.hashCode = 31 * this.hashCode + endpointDescription.hashCode();
	}

	public int hashCode() {
		return hashCode;
	}

	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (!(other instanceof DiscoveredEndpointDescription))
			return false;
		DiscoveredEndpointDescription o = (DiscoveredEndpointDescription) other;
		return (this.discoveryLocatorNamespace
				.equals(o.discoveryLocatorNamespace) && this.endpointDescription
				.equals(o.endpointDescription));
	}

	public Namespace getDiscoveryLocatorNamespace() {
		return discoveryLocatorNamespace;
	}

	public IServiceID getServiceID() {
		return serviceID;
	}
	
	public EndpointDescription getEndpointDescription() {
		return endpointDescription;
	}
}
