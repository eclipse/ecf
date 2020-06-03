/****************************************************************************
 * Copyright (c) 2009 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteservice.soap.client;

import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.client.*;

/**
 * Factory for creating {@link IRemoteCallable} instances.
 * 
 * @since 3.3
 */
public class SoapCallableFactory {

	public static IRemoteCallable createCallable(String method, String resourcePath, IRemoteCallParameter[] defaultParameters, IRemoteCallableRequestType requestType, long defaultTimeout) {
		return new RemoteCallable(method, resourcePath, defaultParameters, requestType, defaultTimeout);
	}

	public static IRemoteCallable createCallable(String method, String resourcePath, IRemoteCallParameter[] defaultParameters, IRemoteCallableRequestType requestType) {
		return createCallable(method, resourcePath, defaultParameters, requestType, IRemoteCall.DEFAULT_TIMEOUT);
	}

	public static IRemoteCallable createCallable(String method, String resourcePath, IRemoteCallParameter[] defaultParameters) {
		return createCallable(method, resourcePath, defaultParameters, new SoapCallableRequestType(), IRemoteCall.DEFAULT_TIMEOUT);
	}

	public static IRemoteCallable createCallable(String method, String resourcePath) {
		return createCallable(method, resourcePath, null, new SoapCallableRequestType(), IRemoteCall.DEFAULT_TIMEOUT);
	}

	public static IRemoteCallable createCallable(String method) {
		return createCallable(method, method, null, new SoapCallableRequestType(), IRemoteCall.DEFAULT_TIMEOUT);
	}

	public static IRemoteCallable createCallable(String method, String resourcePath, SoapCallableRequestType requestType, long timeout) {
		return createCallable(method, resourcePath, null, requestType, timeout);
	}

	public static IRemoteCallable createCallable(String method, String resourcePath, SoapCallableRequestType requestType) {
		return createCallable(method, resourcePath, null, requestType, IRemoteCall.DEFAULT_TIMEOUT);
	}

}
