package org.eclipse.ecf.remoteservice.rest;

import java.io.IOException;
import java.text.ParseException;

import org.apache.commons.httpclient.HttpMethod;
import org.eclipse.ecf.remoteservice.rest.resource.IRestResource;

public interface IRestResourceRepresentationFactory {

	/**
	 * Creates a resource representation for the resource defined in
	 * {@link IRestCall}'s getEstimatedResourceIdentifier() Method. This will be
	 * compared with all registered services of the type {@link IRestResource}
	 * by calling their getIdentifier() methods. If a service matches the
	 * estimated identifier it's parse method will be invoked to parse the
	 * content of the resource.
	 */
	public abstract Object createResourceRepresentation(HttpMethod method, IRestCall restCall) throws ParseException,
			IOException;
}