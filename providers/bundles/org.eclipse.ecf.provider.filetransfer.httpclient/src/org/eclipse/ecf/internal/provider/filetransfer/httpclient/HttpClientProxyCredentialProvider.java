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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.eclipse.ecf.core.util.Proxy;

public abstract class HttpClientProxyCredentialProvider implements CredentialsProvider {

	abstract protected Proxy getECFProxy();

	abstract protected Credentials getNTLMCredentials(Proxy proxy);

	private Collection provided;

	public HttpClientProxyCredentialProvider() {
		provided = new HashSet();
	}

	private Object makeProvidedKey(AuthScheme scheme, String host, int port, boolean isProxyAuthenticating) {
		ArrayList list = new ArrayList(3);
		list.add(host);
		list.add(new Integer(port));
		list.add(Boolean.valueOf(isProxyAuthenticating));
		return list;
	}

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

		Object provideKey = makeProvidedKey(scheme, host, port, isProxyAuthenticating);
		if (provided.contains(provideKey)) {
			// HttpClient asks about credentials only once.
			// If already provided don't use them again.
			return null;
		}

		provided.add(provideKey);

		if ("ntlm".equalsIgnoreCase(scheme.getSchemeName())) { //$NON-NLS-1$
			return getNTLMCredentials(proxy);
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
