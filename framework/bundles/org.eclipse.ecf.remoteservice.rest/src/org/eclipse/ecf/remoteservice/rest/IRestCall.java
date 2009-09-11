/******************************************************************************* 
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.remoteservice.rest;

import java.net.URI;
import java.util.Map;

import org.apache.commons.httpclient.methods.RequestEntity;
import org.eclipse.ecf.remoteservice.rest.util.RestCallFactory;

/**
 * This class acts as a container for a specific REST service call. It can be
 * implemented from scratch or created via {@link RestCallFactory}.
 */
public interface IRestCall {

	/**
	 * Http GET method.
	 */
	public static final String HTTP_GET = "GET";
	/**
	 * Http POST method.
	 */
	public static final String HTTP_POST = "POST";
	/**
	 * Http PUT method.
	 */
	public static final String HTTP_PUT = "PUT";
	/**
	 * Htpp DELETE method.
	 */
	public static final String HTTP_DELETE = "DELETE";

	/**
	 * Every rest web service returns a resource representation. I.e a
	 * representation as a xml file from a xml resource.
	 * 
	 * @return the identifier for a estimated resource, i.e.
	 *         ecf.rest.resource.xml. Will not be <code>null</code>.
	 */
	public String getEstimatedResourceIdentifier();

	/**
	 * Defines the URI for the rest service. This should be a unique URI which
	 * belongs to a rest service i.e on the web.
	 * 
	 * @return the URI of a rest service. Will not be <code>null</code>.
	 */
	public URI getURI();

	/**
	 * Defines the HTTP method for this type of rest call. For example a GET or
	 * POST method.
	 * 
	 * @return the HTTP Method as one of the IRestCall HTTP_* constants. Will
	 *         not be <code>null</code>.
	 */
	public String getMethod();

	/**
	 * Defines the call specific request headers.
	 * 
	 * @return a {@link Map} object which contains the header parameters. May be
	 *         <code>null</code>.
	 */
	public Map getRequestHeaders();

	/**
	 * Defines the key-value pairs parameter for this call. A Rest Call may
	 * either have parameters or a RequestEntity (mutual exclusive).
	 * 
	 * @return an array containing key-value pairs in the format: key=value. May
	 *         be <code>null</code>.
	 * 
	 * @see #getRequestEntity()
	 */
	public Object[] getParameters();

	/**
	 * Get timeout (in ms) for the rest call.
	 * 
	 * @return long timeout in ms
	 */
	public long getTimeout();

	/**
	 * Gets the request entity. The RequestEntity is typically used to transmit
	 * data by HTTP_POST and HTTP_PUT calls. HTTP_GET calls may not use a
	 * request entity A. Rest Call may either have parameters or a RequestEntity
	 * (mutual exclusive)
	 * 
	 * @return the request entity
	 * 
	 * @see RequestEntity
	 */
	public RequestEntity getRequestEntity();

}
