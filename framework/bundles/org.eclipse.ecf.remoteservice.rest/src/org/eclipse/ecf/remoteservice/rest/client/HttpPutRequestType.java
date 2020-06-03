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

import java.util.Map;

public class HttpPutRequestType extends AbstractEntityRequestType {

	public HttpPutRequestType(int requestEntityType, String defaultContentType, long defaultContentLength, String defaultCharset, Map defaultRequestHeaders) {
		super(requestEntityType, defaultContentType, defaultContentLength, defaultCharset, defaultRequestHeaders);
	}

	public HttpPutRequestType(int requestEntityType, String defaultContentType, long defaultContentLength, String defaultCharset) {
		super(requestEntityType, defaultContentType, defaultContentLength, defaultCharset);
	}

	public HttpPutRequestType(int requestEntityType, String defaultContentType, long defaultContentLength, Map defaultRequestHeaders) {
		super(requestEntityType, defaultContentType, defaultContentLength, defaultRequestHeaders);
	}

	public HttpPutRequestType(int requestEntityType, String defaultContentType, long defaultContentLength) {
		super(requestEntityType, defaultContentType, defaultContentLength);
	}

	public HttpPutRequestType(int requestEntityType, String defaultContentType, Map defaultRequestHeaders) {
		super(requestEntityType, defaultContentType, defaultRequestHeaders);
	}

	public HttpPutRequestType(int requestEntityType, String defaultContentType) {
		super(requestEntityType, defaultContentType);
	}

	public HttpPutRequestType(int requestEntityType, Map defaultRequestHeaders) {
		super(requestEntityType, defaultRequestHeaders);
	}

	public HttpPutRequestType(Map defaultRequestHeaders) {
		super(defaultRequestHeaders);
	}

	public HttpPutRequestType() {
		// nothing
	}

}
