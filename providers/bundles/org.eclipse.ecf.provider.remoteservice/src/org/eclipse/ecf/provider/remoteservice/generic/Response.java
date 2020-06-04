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

public class Response implements Serializable {

	private static final long serialVersionUID = 634820397523983872L;

	long requestId;

	Object response;

	Throwable exception;

	public Response(long requestId, Object response) {
		this.requestId = requestId;
		this.response = response;
	}

	public Response(long requestId, Throwable exception) {
		this.requestId = requestId;
		this.exception = exception;
	}

	public long getRequestId() {
		return requestId;
	}

	public Object getResponse() {
		return response;
	}

	public boolean hadException() {
		return (exception != null);
	}

	public Throwable getException() {
		return exception;
	}

	public String toString() {
		final StringBuffer buf = new StringBuffer("Response["); //$NON-NLS-1$
		buf.append("requestId=").append(requestId).append(";response=").append( //$NON-NLS-1$ //$NON-NLS-2$
				response).append(";exception=").append(exception).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		return buf.toString();
	}
}
