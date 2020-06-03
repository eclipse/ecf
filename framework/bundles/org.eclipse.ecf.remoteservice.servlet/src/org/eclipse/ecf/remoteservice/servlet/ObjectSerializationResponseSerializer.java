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
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.ecf.remoteservice.util.ObjectSerializationUtil;

public class ObjectSerializationResponseSerializer extends
		ObjectSerializationUtil implements IRemoteCallResponseSerializer {

	public void serializeResponse(HttpServletResponse resp,
			Object responseObject) throws IOException, ServletException {
		if (responseObject == null) return;
		byte[] bytes = serializeToBytes(responseObject);
		OutputStream outs = resp.getOutputStream();
		writeByteArray(outs, bytes);
	}

}
