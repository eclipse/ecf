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

import org.eclipse.ecf.remoteservice.util.ObjectSerializationUtil;

public class ObjectSerializationParameterDeserializer extends ObjectSerializationUtil implements
		IRemoteCallParameterDeserializer {

	public Object[] deserializeParameters(HttpServletRequest req)
			throws IOException, ServletException {
		byte [] inputStreamAsBytes = readToByteArray(req.getInputStream());
		Object object = deserializeFromBytes(inputStreamAsBytes);
		if (object instanceof Object[]) return (Object[]) object;
		return new Object[] { object };
	}

}
