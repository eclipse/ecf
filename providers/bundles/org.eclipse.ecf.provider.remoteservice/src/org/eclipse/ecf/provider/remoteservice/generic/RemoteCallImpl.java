/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.provider.remoteservice.generic;

import java.io.Serializable;
import java.util.Arrays;
import org.eclipse.ecf.core.sharedobject.SharedObjectMsg;
import org.eclipse.ecf.remoteservice.IRemoteCall;

public class RemoteCallImpl extends SharedObjectMsg implements IRemoteCall, Serializable {

	private static final long serialVersionUID = 1L;

	long timeout = IRemoteCall.DEFAULT_TIMEOUT;

	/**
	 * @param clazz the class
	 * @param method the method
	 * @param parameters the parameters
	 * @param timeout timeout
	 * @return RemoteCallImpl created remote call
	 * @since 4.0
	 */
	public static RemoteCallImpl createRemoteCall(String clazz, String method, Object[] parameters, long timeout) {
		return new RemoteCallImpl(clazz, method, parameters, timeout);
	}

	protected RemoteCallImpl(String clazz, String method, Object[] parameters, long timeout) {
		super(clazz, method, parameters);
		this.timeout = timeout;
	}

	public long getTimeout() {
		return timeout;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("RemoteCallImpl["); //$NON-NLS-1$
		synchronized (buf) {
			buf.append("class=").append(clazz).append(';'); //$NON-NLS-1$
			buf.append("method=").append(method).append(';'); //$NON-NLS-1$
			buf.append("params=") //$NON-NLS-1$
					.append(parameters == null ? "" : Arrays.asList(parameters) //$NON-NLS-1$
							.toString())
					.append(';');
			buf.append("timeout=").append(timeout).append(']'); //$NON-NLS-1$
		}
		return buf.toString();
	}
}
