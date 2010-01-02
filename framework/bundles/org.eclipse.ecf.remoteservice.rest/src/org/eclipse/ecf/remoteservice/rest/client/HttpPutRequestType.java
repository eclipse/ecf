/*******************************************************************************
* Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.remoteservice.rest.client;

import java.util.Map;
import org.eclipse.ecf.remoteservice.rest.util.IRequestEntity;

public class HttpPutRequestType extends AbstractRestRequestType {

	private IRequestEntity requestEntity = null;

	public HttpPutRequestType(IRequestEntity requestEntity, Map defaultRequestHeaders) {
		super(defaultRequestHeaders);
		this.requestEntity = requestEntity;
	}

	public HttpPutRequestType(IRequestEntity requestEntity) {
		super(null);
		this.requestEntity = requestEntity;
	}

	public HttpPutRequestType(Map defaultRequestHeaders) {
		super(defaultRequestHeaders);
	}

	public HttpPutRequestType() {
		// nothing to do
	}

	public IRequestEntity getRequestEntity() {
		return requestEntity;
	}
}
