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
package org.eclipse.ecf.remoteservice;

import java.util.Arrays;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.remoteservice.asyncproxy.AbstractAsyncProxyRemoteCall;

/**
 * @since 4.0
 */
public class RemoteCall extends AbstractAsyncProxyRemoteCall implements IRemoteCall {

	protected String method;
	protected Object[] parameters;
	protected long timeout;

	public RemoteCall(String method, Object[] parameters, long timeout) {
		this.method = method;
		Assert.isNotNull(this.method);
		this.parameters = parameters;
		this.timeout = timeout;
	}

	public RemoteCall(String method, Object[] parameters) {
		this(method, parameters, DEFAULT_TIMEOUT);
	}

	public RemoteCall(String method) {
		this(method, null);
	}

	public String getMethod() {
		return method;
	}

	public Object[] getParameters() {
		return parameters;
	}

	public long getTimeout() {
		return timeout;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("RemoteCall[method="); //$NON-NLS-1$
		buffer.append(method);
		buffer.append(", parameters="); //$NON-NLS-1$
		buffer.append(parameters != null ? Arrays.asList(parameters) : null);
		buffer.append(", timeout="); //$NON-NLS-1$
		buffer.append(timeout);
		buffer.append("]"); //$NON-NLS-1$
		return buffer.toString();
	}

}
