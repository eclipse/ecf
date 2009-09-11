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
 * This class acts as the super class for the service object {@link GetRestCall},
 * {@link PostRestCall}, {@link DeleteRestCall} and {@link PutRestCall}. Sub classes
 * may override the methods from {@link IRestCall}.
 */
public class RestCall implements IRestCall {

	private long timeout;
	private Map requestHeaders;
	private Object[] params;
	private RequestEntity requestEntity;
	private String resourceIdentifier;
	private URI uri;
	private String method;

	
	protected RestCall(URI uri, String resourceIdentifier, Object[] params, RequestEntity requestEntity, Map requestHeaders, long timeout) {
		this.uri = uri;
		this.resourceIdentifier = resourceIdentifier;
		this.params = params;
		this.requestEntity = requestEntity;
		this.requestHeaders = requestHeaders;
		this.timeout = timeout;
	}
	
	protected RestCall(String method, URI uri, String resourceIdentifier, Object[] params, RequestEntity requestEntity, Map requestHeaders, long timeout) {
		this(uri, resourceIdentifier, params, requestEntity, requestHeaders, timeout);
		this.method = method;
	}

	public String getEstimatedResourceIdentifier() {
		return resourceIdentifier;
	}

	public String getMethod() {
		return method;
	}

	public Object[] getParameters() {
		return params;
	}

	public Map getRequestHeaders() {
		return requestHeaders;
	}

	public long getTimeout() {
		return timeout;
	}

	public URI getURI() {
		return uri;
	}

	public RequestEntity getRequestEntity() {
		return requestEntity;
	}

	
}
