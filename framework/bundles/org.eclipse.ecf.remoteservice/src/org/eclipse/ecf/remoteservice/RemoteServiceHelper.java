/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.remoteservice;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.IFuture;

/**
 * Helper class for making it easier to call a remote service with method name and optional parameters.
 * 
 * @since 3.0
 */
public class RemoteServiceHelper {

	public static long defaultTimeout = 30000; // Default of 30 seconds.

	public static long getDefaultTimeout() {
		return defaultTimeout;
	}

	public static void setDefaultTimeout(long timeout) {
		defaultTimeout = timeout;
	}

	public static void asyncExec(IRemoteService remoteService, final String method, final Object[] parameters, IRemoteCallListener listener) {
		asyncExec(remoteService, method, parameters, getDefaultTimeout(), listener);
	}

	public static void asyncExec(IRemoteService remoteService, final String method, final Object[] parameters, final long timeout, IRemoteCallListener listener) {
		Assert.isNotNull(remoteService);
		Assert.isNotNull(method);
		Assert.isNotNull(listener);
		remoteService.callAsync(new IRemoteCall() {
			public String getMethod() {
				return method;
			}

			public Object[] getParameters() {
				return parameters;
			}

			public long getTimeout() {
				return timeout;
			}
		}, listener);
	}

	public static IFuture futureExec(IRemoteService remoteService, final String method, final Object[] parameters, final long timeout) {
		Assert.isNotNull(remoteService);
		Assert.isNotNull(method);
		return remoteService.callAsync(new IRemoteCall() {
			public String getMethod() {
				return method;
			}

			public Object[] getParameters() {
				return parameters;
			}

			public long getTimeout() {
				return timeout;
			}
		});
	}

	public static IFuture futureExec(IRemoteService remoteService, final String method, final Object[] parameters) {
		return futureExec(remoteService, method, parameters, getDefaultTimeout());
	}

	public static Object syncExec(IRemoteService remoteService, final String method, final Object[] parameters, final long timeout) throws ECFException {
		Assert.isNotNull(remoteService);
		Assert.isNotNull(method);
		return remoteService.callSync(new IRemoteCall() {

			public String getMethod() {
				return method;
			}

			public Object[] getParameters() {
				return parameters;
			}

			public long getTimeout() {
				return timeout;
			}
		});
	}

	public static Object syncExec(IRemoteService remoteService, final String method, final Object[] parameters) throws ECFException {
		return syncExec(remoteService, method, parameters, getDefaultTimeout());
	}
}
