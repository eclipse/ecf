/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.internal.provider.filetransfer.httpclient;

import org.eclipse.ecf.provider.filetransfer.httpclient.HttpClientRetrieveFileTransfer;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.eclipse.ecf.core.util.Proxy;

public abstract class HttpClientProxyCredentialProvider implements CredentialsProvider {

	abstract protected Proxy getECFProxy();

	/**
	 * @throws CredentialsNotAvailableException  
	 */
	public Credentials getCredentials(AuthScheme scheme, String host, int port, boolean isProxyAuthenticating) throws CredentialsNotAvailableException {
		if (!isProxyAuthenticating) {
			return null;
		}
		Proxy proxy = getECFProxy();
		if (proxy == null) {
			return null;
		}
		if ("ntlm".equalsIgnoreCase(scheme.getSchemeName())) { //$NON-NLS-1$
			return HttpClientRetrieveFileTransfer.createNTLMCredentials(proxy);
		} else if ("basic".equalsIgnoreCase(scheme.getSchemeName()) || //$NON-NLS-1$
				"digest".equalsIgnoreCase(scheme.getSchemeName())) { //$NON-NLS-1$
			final String proxyUsername = proxy.getUsername();
			final String proxyPassword = proxy.getPassword();
			if (proxyUsername != null) {
				Credentials credentials = new UsernamePasswordCredentials(proxyUsername, proxyPassword);
				return credentials;
			}
		}

		return null;
	}
}
