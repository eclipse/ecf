/*******************************************************************************
* Copyright (c) 2014 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.remoteservice.client;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.remoteservice.IRemoteCall;

/**
 * @since 8.5
 */
public class RemoteCallableBuilder {

	private final String methodName;
	private final String resourcePath;
	private IRemoteCallParameter[] defaultParameters;
	private long timeout = IRemoteCall.DEFAULT_TIMEOUT;
	private IRemoteCallableRequestType requestType;

	public RemoteCallableBuilder(String methodName, String resourcePath) {
		Assert.isNotNull(methodName);
		this.methodName = methodName;
		this.resourcePath = resourcePath;
		Assert.isNotNull(resourcePath);
	}

	public RemoteCallableBuilder setDefaultParameters(IRemoteCallParameter[] defaultParameters) {
		this.defaultParameters = defaultParameters;
		return this;
	}

	public RemoteCallableBuilder setRequestTimeout(long timeout) {
		this.timeout = timeout;
		return this;
	}

	public RemoteCallableBuilder setRequestType(IRemoteCallableRequestType requestType) {
		this.requestType = requestType;
		return this;
	}

	public IRemoteCallable build() {
		return new RemoteCallable(this.methodName, this.resourcePath, this.defaultParameters, this.requestType, this.timeout);
	}
}
