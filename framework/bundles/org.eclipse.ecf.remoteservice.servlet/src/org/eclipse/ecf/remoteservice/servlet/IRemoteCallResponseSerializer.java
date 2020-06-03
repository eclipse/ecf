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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

/**
 * Remote call response serializer.
 *
 */
public interface IRemoteCallResponseSerializer {

	/**
	 * Serialize responseObject to HttpServletResponse
	 * 
	 * @param resp the HttpServletResponse object.  Will not be <code>null</code>.
	 * @param responseObject to serialize to resp
	 * @throws IOException if resposeObject cannot be serialized
	 * @throws ServletException if responseObject cannot be serialized
	 */
	public void serializeResponse(HttpServletResponse resp, Object responseObject) throws IOException, ServletException;
	
}
