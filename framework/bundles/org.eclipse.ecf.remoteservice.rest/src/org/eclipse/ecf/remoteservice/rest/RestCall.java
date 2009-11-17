/******************************************************************************* 
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *   Composent, Inc - Simplifications
 *******************************************************************************/
package org.eclipse.ecf.remoteservice.rest;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import org.eclipse.core.runtime.Assert;

/**
 * Implementation of {@link IRestCall}.  Note that {@link RestCallFactory} should 
 * typically be used to construct instances.
 */
public class RestCall implements IRestCall, Serializable {

	private static final long serialVersionUID = -2688657222934833060L;

	private String method;
	private Object[] params;
	private Map requestHeaders;
	private long timeout = IRestCallable.DEFAULT_TIMEOUT;

	public RestCall(String fqMethod, Object[] params, Map requestHeaders, long timeout) {
		Assert.isNotNull(fqMethod);
		this.method = fqMethod;
		this.params = params;
		this.requestHeaders = requestHeaders;
		this.timeout = timeout;
	}

	public RestCall(String fqMethod, Object[] params, Map requestHeaders) {
		this(fqMethod, params, requestHeaders, IRestCallable.DEFAULT_TIMEOUT);
	}

	public RestCall(String fqMethod, Object[] params) {
		this(fqMethod, params, null);
	}

	public RestCall(String fqMethod) {
		this(fqMethod, null);
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

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("RestCall["); //$NON-NLS-1$
		buffer.append("method="); //$NON-NLS-1$
		buffer.append(method);
		buffer.append(", params="); //$NON-NLS-1$
		buffer.append(params != null ? Arrays.asList(params) : null);
		buffer.append(", requestHeaders="); //$NON-NLS-1$
		buffer.append(requestHeaders);
		buffer.append(", timeout="); //$NON-NLS-1$
		buffer.append(timeout);
		buffer.append("]"); //$NON-NLS-1$
		return buffer.toString();
	}

}
