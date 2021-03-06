/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.provider.filetransfer.httpclient4;

import javax.net.ssl.X509TrustManager;

public class HttpClientSslTrustManager implements X509TrustManager {
	// seems to be no purpose
	public boolean checkClientTrusted(java.security.cert.X509Certificate[] chain) {
		return true;
	}

	// seems to be no purpose
	public boolean isServerTrusted(java.security.cert.X509Certificate[] chain) {
		return true;
	}

	// seems to be no purpose
	public boolean isClientTrusted(java.security.cert.X509Certificate[] chain) {
		return true;
	}

	/**
	 * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
	 */
	public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		return null;
	}

	/**
	 * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[],
	 *      java.lang.String)
	 */
	public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
			String authType) {
		// don't need to do any checks
	}

	/**
	 * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[],
	 *      java.lang.String)
	 */
	public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
			String authType) {
		// don't need to do any checks
	}

}
