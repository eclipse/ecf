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

public class HttpGetRequestType extends AbstractRequestType {

	public HttpGetRequestType(Map defaultRequestHeaders) {
		super(defaultRequestHeaders);
	}

	public HttpGetRequestType() {
		// nothing to do
	}
}
