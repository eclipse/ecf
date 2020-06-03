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
package org.eclipse.ecf.remoteservice.client;

import org.eclipse.ecf.remoteservice.IRemoteCall;

/**
 * Factory for creating {@link IRemoteCallable} instances.
 * 
 * @since 4.0
 */
public class RemoteCallableFactory {

	public static IRemoteCallable createCallable(String method, String resourcePath, IRemoteCallParameter[] defaultParameters, IRemoteCallableRequestType requestType, long defaultTimeout) {
		return new RemoteCallable(method, resourcePath, defaultParameters, requestType, defaultTimeout);
	}

	public static IRemoteCallable createCallable(String method, String resourcePath, IRemoteCallParameter[] defaultParameters, IRemoteCallableRequestType requestType) {
		return createCallable(method, resourcePath, defaultParameters, requestType, IRemoteCall.DEFAULT_TIMEOUT);
	}

	public static IRemoteCallable createCallable(String method, String resourcePath, IRemoteCallParameter[] defaultParameters) {
		return createCallable(method, resourcePath, defaultParameters, null, IRemoteCall.DEFAULT_TIMEOUT);
	}

	public static IRemoteCallable createCallable(String method, String resourcePath) {
		return createCallable(method, resourcePath, null, null, IRemoteCall.DEFAULT_TIMEOUT);
	}

	public static IRemoteCallable createCallable(String method) {
		return createCallable(method, method, null, null, IRemoteCall.DEFAULT_TIMEOUT);
	}

	public static IRemoteCallable createCallable(String method, String resourcePath, IRemoteCallableRequestType requestType, long timeout) {
		return createCallable(method, resourcePath, null, requestType, timeout);
	}

	public static IRemoteCallable createCallable(String method, String resourcePath, IRemoteCallableRequestType requestType) {
		return createCallable(method, resourcePath, null, requestType, IRemoteCall.DEFAULT_TIMEOUT);
	}

}
