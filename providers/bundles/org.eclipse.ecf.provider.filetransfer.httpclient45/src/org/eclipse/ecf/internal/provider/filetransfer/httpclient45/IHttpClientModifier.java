/****************************************************************************
 * Copyright (c) 2019 Yatta Solutions and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Yatta Solutions - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.filetransfer.httpclient45;

import java.util.Map;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;

public interface IHttpClientModifier {
	HttpClientBuilder modifyClient(HttpClientBuilder builder);

	CredentialsProvider modifyCredentialsProvider(CredentialsProvider credentialsProvider);

	HttpClientContext modifyContext(HttpClientContext context);

	RequestConfig.Builder modifyRequestConfig(RequestConfig.Builder config, HttpClientContext context, Map<?, ?> options);
}
