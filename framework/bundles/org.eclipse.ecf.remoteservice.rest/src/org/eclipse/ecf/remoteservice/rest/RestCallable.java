/*******************************************************************************
* Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.remoteservice.rest;

import java.util.Arrays;
import java.util.Map;
import org.eclipse.core.runtime.Assert;

public class RestCallable implements IRestCallable {

	private String method;
	private String resourcePath;
	private IRestParameter[] parameters;
	private long defaultTimeout;
	private RequestType requestType;
	private Map requestHeaders = null;

	public RestCallable(String method, String resourcePath, IRestParameter[] parameters, RequestType requestType, Map requestHeaders, long timeout) {
		Assert.isNotNull(method);
		this.method = method;
		Assert.isNotNull(resourcePath);
		this.resourcePath = resourcePath;
		this.parameters = parameters;
		this.requestType = (requestType == null) ? DEFAULT_REQUEST_TYPE : requestType;
		this.requestHeaders = requestHeaders;
		this.defaultTimeout = timeout;
	}

	public RestCallable(String method, String resourcePath, IRestParameter[] parameters, RequestType requestType, Map requestHeaders) {
		this(method, resourcePath, parameters, requestType, requestHeaders, DEFAULT_TIMEOUT);
	}

	public RestCallable(String method, String resourcePath, IRestParameter[] parameters, RequestType requestType) {
		this(method, resourcePath, parameters, requestType, null, DEFAULT_TIMEOUT);
	}

	public RestCallable(String method, String resourcePath, IRestParameter[] parameters) {
		this(method, resourcePath, parameters, DEFAULT_REQUEST_TYPE, null, DEFAULT_TIMEOUT);
	}

	public RestCallable(String method, String resourcePath) {
		this(method, resourcePath, null, DEFAULT_REQUEST_TYPE, null, DEFAULT_TIMEOUT);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.remoteservice.rest.IRestCallable#getMethod()
	 */
	public String getMethod() {
		return method;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.remoteservice.rest.IRestCallable#getResourcePath()
	 */
	public String getResourcePath() {
		return resourcePath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.remoteservice.rest.IRestCallable#getParameters()
	 */
	public IRestParameter[] getParameters() {
		return parameters;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.remoteservice.rest.IRestCallable#getDefaultTimeout()
	 */
	public long getDefaultTimeout() {
		return defaultTimeout;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.remoteservice.rest.IRestCallable#getRequestType()
	 */
	public RequestType getRequestType() {
		return requestType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.remoteservice.rest.IRestCallable#getDefaultRequestHeaders()
	 */
	public Map getDefaultRequestHeaders() {
		return requestHeaders;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("RestCallable[method="); //$NON-NLS-1$
		buffer.append(method);
		buffer.append(", resourcePath="); //$NON-NLS-1$
		buffer.append(resourcePath);
		buffer.append(", parameters="); //$NON-NLS-1$
		buffer.append(parameters != null ? Arrays.asList(parameters) : null);
		buffer.append(", defaultTimeout="); //$NON-NLS-1$
		buffer.append(defaultTimeout);
		buffer.append(", requestType="); //$NON-NLS-1$
		buffer.append(requestType);
		buffer.append(", requestHeaders="); //$NON-NLS-1$
		buffer.append(requestHeaders);
		buffer.append("]"); //$NON-NLS-1$
		return buffer.toString();
	}

}
