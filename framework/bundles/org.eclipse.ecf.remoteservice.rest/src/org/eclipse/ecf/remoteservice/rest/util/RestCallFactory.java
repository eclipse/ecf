/******************************************************************************* 
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.remoteservice.rest.util;

import java.net.URI;
import java.util.Map;

import org.apache.commons.httpclient.methods.RequestEntity;
import org.eclipse.ecf.remoteservice.rest.IRestCall;

/**
 * Factory to support the creation of {@link IRestCall} objects.
 */
public class RestCallFactory {

	/**
	 * Creates a specific {@link IRestCall}.
	 * 
	 * @param method
	 *            The type of the http method. GET, PUT, DELETE or POST.
	 * @param uri
	 *            {@link IRestCall#getURI()}
	 * @param resourceIdentifier
	 *            {@link IRestCall#getEstimatedResourceIdentifier()}
	 * @param params
	 *            {@link IRestCall#getParameters()}
	 * @param requestHeaders
	 *            {@link IRestCall#getRequestHeaders()}
	 * @param timeout
	 *            {@link IRestCall#getTimeout()}
	 * 
	 * @return {@link IRestCall} object filled with the overgiven params. Will
	 *         not be <code>null</code>.
	 */
	public static IRestCall createRestCall(final String method, final URI uri, final String resourceIdentifier,
			final Object[] params, final Map requestHeaders, final long timeout) {

		return createRestCall(method, uri, resourceIdentifier, params, null, requestHeaders, timeout);
	}

	/**
	 * Creates a specific {@link IRestCall}.
	 * 
	 * @param method
	 *            The type of the http method. GET, PUT, DELETE or POST.
	 * @param uri
	 *            {@link IRestCall#getURI()}
	 * @param resourceIdentifier
	 *            {@link IRestCall#getEstimatedResourceIdentifier()}
	 * @param requestEntity
	 *            {@link IRestCall#getRequestEntity()}
	 * @param requestHeaders
	 *            {@link IRestCall#getRequestHeaders()}
	 * @param timeout
	 *            {@link IRestCall#getTimeout()}
	 * 
	 * @return {@link IRestCall} object filled with the overgiven params. Will
	 *         not be <code>null</code>.
	 */
	public static IRestCall createRestCall(final String method, final URI uri, final String resourceIdentifier,
			final RequestEntity requestEntity, final Map requestHeaders, final long timeout) {
		return createRestCall(method, uri, resourceIdentifier, null, requestEntity, requestHeaders, timeout);
	}

	/**
	 * Creates a specific {@link IRestCall}.
	 * 
	 * @param method
	 *            The type of the http method. GET, PUT, DELETE or POST.
	 * @param uri
	 *            {@link IRestCall#getURI()}
	 * @param resourceIdentifier
	 *            {@link IRestCall#getEstimatedResourceIdentifier()}
	 * @param requestHeaders
	 *            {@link IRestCall#getRequestHeaders()}
	 * @param timeout
	 *            {@link IRestCall#getTimeout()}
	 * 
	 * @return {@link IRestCall} object filled with the overgiven params. Will
	 *         not be <code>null</code>.
	 */
	public static IRestCall createRestCall(final String method, final URI uri, final String resourceIdentifier,
			final Map requestHeaders, final long timeout) {
		return createRestCall(method, uri, resourceIdentifier, null, null, requestHeaders, timeout);
	}

	private static IRestCall createRestCall(final String method, final URI uri, final String resourceIdentifier,
			final Object[] params, final RequestEntity requestEntity, final Map requestHeaders, final long timeout) {

		if (method.equals(IRestCall.HTTP_GET))
			return new GetRestCall(uri, resourceIdentifier, params, requestEntity, requestHeaders, timeout);
		if (method.equals(IRestCall.HTTP_POST))
			return new PostRestCall(uri, resourceIdentifier, params, requestEntity, requestHeaders, timeout);
		if (method.equals(IRestCall.HTTP_DELETE))
			return new DeleteRestCall(uri, resourceIdentifier, params, requestEntity, requestHeaders, timeout);
		if (method.equals(IRestCall.HTTP_PUT))
			return new PutRestCall(uri, resourceIdentifier, params, requestEntity, requestHeaders, timeout);
		return new RestCall(method, uri, resourceIdentifier, params, requestEntity, requestHeaders, timeout);
	}
}
