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
 * Implementation for http PUT.
 */
public class PutRestCall extends RestCall {

	public PutRestCall(URI uri, String resourceIdentifier, Object[] params, RequestEntity requestEntity,
			Map requestHeaders, long timeout) {
		super(uri, resourceIdentifier, params, requestEntity, requestHeaders, timeout);
	}

	public PutRestCall(URI uri, String resourceIdentifier, Map requestHeaders, Object[] params, long timeout) {
		this(uri, resourceIdentifier, params, null, requestHeaders, timeout);
	}

	public PutRestCall(URI uri, String resourceIdentifier, RequestEntity requestEntity, Map requestHeaders, long timeout) {
		this(uri, resourceIdentifier, null, requestEntity, requestHeaders, timeout);
	}

	public PutRestCall(URI uri, String resourceIdentifier, Map requestHeaders, long timeout) {
		this(uri, resourceIdentifier, null, null, requestHeaders, timeout);
	}

	public String getMethod() {
		return IRestCall.HTTP_PUT;
	}
}
