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
