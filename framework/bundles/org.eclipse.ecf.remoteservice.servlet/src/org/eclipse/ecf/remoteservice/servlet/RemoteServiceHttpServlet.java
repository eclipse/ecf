/****************************************************************************
 * Copyright (c) 2013 Composent, Inc. and others.
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
package org.eclipse.ecf.remoteservice.servlet;

import javax.servlet.http.HttpServlet;

public class RemoteServiceHttpServlet extends HttpServlet {

	private static final long serialVersionUID = -871598533602636840L;

	private IRemoteCallParameterDeserializer parameterDeserializer;
	private IRemoteCallResponseSerializer responseSerializer;
	
	protected void setRemoteCallParameterDeserializer(IRemoteCallParameterDeserializer parameterDeserializer) {
		this.parameterDeserializer = parameterDeserializer;
	}
	
	protected void setRemoteCallResponseSerializer(IRemoteCallResponseSerializer responseSerializer) {
		this.responseSerializer = responseSerializer;
	}
	
	protected IRemoteCallParameterDeserializer getRemoteCallParameterDeserializer() {
		return this.parameterDeserializer;
	}
	
	protected IRemoteCallResponseSerializer getRemoteCallResponseSerializer() {
		return this.responseSerializer;
	}
	
}
