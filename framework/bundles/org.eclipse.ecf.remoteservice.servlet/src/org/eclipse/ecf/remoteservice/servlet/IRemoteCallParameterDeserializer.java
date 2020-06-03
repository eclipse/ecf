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
import javax.servlet.http.HttpServletRequest;

/**
 * Remote call parameter deserializer.
 *
 */
public interface IRemoteCallParameterDeserializer {

	/**
	 * Deserialize parameters from HttpServletRequest.
	 * 
	 * @param req the HttpServletRequest.  Will not be <code>null</code>.
	 * @return Object[] the deserialized parameters.
	 * @throws IOException if parameters cannot be deserialized
	 * @throws ServletException if parameters cannot be deserialized
	 */
	public Object[] deserializeParameters(HttpServletRequest req) throws IOException, ServletException;
	
}
