/****************************************************************************
 * Copyright (c) 2019, 2022 Yatta Solutions and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Yatta Solutions - initial API and implementation
 *   Christoph Läubrich - adapt to java http client
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.filetransfer.httpclientjava;

import java.net.Authenticator;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.Map;

public class HttpClientModifierAdapter implements IHttpClientModifier {

	@Override
	public HttpClient.Builder modifyClient(HttpClient.Builder builder) {
		return builder;
	}

	@Override
	public Authenticator modifyCredentialsProvider(Authenticator credentialsProvider) {
		return credentialsProvider;
	}

	@Override
	public IHttpClientContext modifyContext(IHttpClientContext context) {
		return context;
	}

	@Override
	public HttpRequest.Builder modifyRequestConfig(HttpRequest.Builder config, IHttpClientContext context,
			Map<?, ?> options) {
		return config;
	}

}
