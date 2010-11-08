package org.eclipse.ecf.osgi.services.remoteserviceadmin;

public interface IEndpointDescriptionPublisher {

	public boolean publish(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription);

	public boolean unpublish(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription);

}
