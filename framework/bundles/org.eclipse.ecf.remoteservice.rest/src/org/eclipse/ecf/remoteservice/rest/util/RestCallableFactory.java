/*******************************************************************************
* Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.remoteservice.rest.util;


import org.eclipse.ecf.remoteservice.*;

public class RestCallableFactory {

	public static final long DEFAULT_TIMEOUT = new Long(System.getProperty("ecf.remotecall.rest.timeout", "30000")).longValue(); //$NON-NLS-1$ //$NON-NLS-2$

	public static IRemoteCallable createRestCallable(String method, String resourcePath, IRemoteCallParameter[] defaultParameters, RestRequestType requestType, long timeout) {
		return new RemoteCallable(method, resourcePath, defaultParameters, requestType, timeout);
	}

	public static IRemoteCallable createRestCallable(String method, String resourcePath, IRemoteCallParameter[] defaultParameters, RestRequestType requestType) {
		return createRestCallable(method, resourcePath, defaultParameters, requestType, DEFAULT_TIMEOUT);
	}

	public static IRemoteCallable createRestCallable(String method, String resourcePath, IRemoteCallParameter[] defaultParameters) {
		return createRestCallable(method, resourcePath, defaultParameters, new HttpGetRequestType(), DEFAULT_TIMEOUT);
	}

	public static IRemoteCallable createRestCallable(String method, String resourcePath) {
		return createRestCallable(method, resourcePath, null, new HttpGetRequestType(), DEFAULT_TIMEOUT);
	}

	public static IRemoteCallable createRestCallable(String method) {
		return createRestCallable(method, method, null, new HttpGetRequestType(), DEFAULT_TIMEOUT);
	}

	public static IRemoteCallable createRestCallable(String method, String resourcePath, RestRequestType requestType, long timeout) {
		return createRestCallable(method, resourcePath, null, requestType, timeout);
	}

	public static IRemoteCallable createRestCallable(String method, String resourcePath, RestRequestType requestType) {
		return createRestCallable(method, resourcePath, null, requestType, DEFAULT_TIMEOUT);
	}

}
