/*******************************************************************************
* Copyright (c) 2019 Yatta Solutions and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Yatta Solutions - initial API and implementation
******************************************************************************/
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
