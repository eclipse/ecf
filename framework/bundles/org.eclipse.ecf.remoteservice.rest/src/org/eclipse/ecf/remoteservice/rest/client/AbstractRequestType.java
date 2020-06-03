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
package org.eclipse.ecf.remoteservice.rest.client;

import org.eclipse.ecf.remoteservice.client.IRemoteCallableRequestType;

import java.util.Map;

public abstract class AbstractRequestType implements IRemoteCallableRequestType {

	protected Map defaultRequestHeaders;

	public AbstractRequestType(Map defaultRequestHeaders) {
		this.defaultRequestHeaders = defaultRequestHeaders;
	}

	public AbstractRequestType() {
		// nothing to do
	}

	public Map getDefaultRequestHeaders() {
		return defaultRequestHeaders;
	}

}
