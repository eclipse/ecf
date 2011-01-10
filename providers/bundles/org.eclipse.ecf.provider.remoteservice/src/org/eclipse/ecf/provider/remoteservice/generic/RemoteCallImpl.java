/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.remoteservice.generic;

import java.io.Serializable;
import java.util.Arrays;
import org.eclipse.ecf.core.sharedobject.SharedObjectMsg;
import org.eclipse.ecf.remoteservice.IRemoteCall;

public class RemoteCallImpl extends SharedObjectMsg implements IRemoteCall, Serializable {

	private static final long serialVersionUID = 1L;

	long timeout = IRemoteCall.DEFAULT_TIMEOUT;

	/**
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
			buf.append("params=").append( //$NON-NLS-1$
					parameters == null ? "" : Arrays.asList(parameters) //$NON-NLS-1$
							.toString()).append(';');
			buf.append("timeout=").append(timeout).append(']'); //$NON-NLS-1$
		}
		return buf.toString();
	}
}
