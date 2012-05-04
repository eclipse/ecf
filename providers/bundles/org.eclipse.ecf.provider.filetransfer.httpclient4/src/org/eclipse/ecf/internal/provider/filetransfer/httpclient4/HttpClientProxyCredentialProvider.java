/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thomas Joiner - HttpClient 4 implementation
 *******************************************************************************/
package org.eclipse.ecf.internal.provider.filetransfer.httpclient4;

import java.util.HashMap;
import java.util.Map;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.eclipse.ecf.core.util.Proxy;
import org.eclipse.ecf.core.util.Trace;

public abstract class HttpClientProxyCredentialProvider implements CredentialsProvider {

	abstract protected Proxy getECFProxy();

	abstract protected Credentials getNTLMCredentials(Proxy proxy);

	private Map cachedCredentials;

	public HttpClientProxyCredentialProvider() {
		cachedCredentials = new HashMap();
	}

	public void setCredentials(AuthScope authscope, Credentials credentials) {
		this.cachedCredentials.put(authscope, credentials);
	}

	public Credentials getCredentials(AuthScope authscope) {
		Trace.entering(Activator.PLUGIN_ID, DebugOptions.METHODS_ENTERING, HttpClientProxyCredentialProvider.class, "getCredentials " + authscope); //$NON-NLS-1$

		if (this.cachedCredentials.containsKey(authscope)) {
			return (Credentials) this.cachedCredentials.get(authscope);
		}

		Proxy proxy = getECFProxy();
		if (proxy == null) {
			return null;
		}

		Credentials credentials = null;
		if ("ntlm".equalsIgnoreCase(authscope.getScheme())) { //$NON-NLS-1$
			credentials = getNTLMCredentials(proxy);
		} else if ("basic".equalsIgnoreCase(authscope.getScheme()) || //$NON-NLS-1$
				"digest".equalsIgnoreCase(authscope.getScheme())) { //$NON-NLS-1$
			final String proxyUsername = proxy.getUsername();
			final String proxyPassword = proxy.getPassword();
			if (proxyUsername != null) {
				credentials = new UsernamePasswordCredentials(proxyUsername, proxyPassword);
			}
		} else if ("negotiate".equalsIgnoreCase(authscope.getScheme())) { //$NON-NLS-1$
			Trace.trace(Activator.PLUGIN_ID, "SPNEGO is not supported, if you can contribute support, please do so."); //$NON-NLS-1$
		} else {
			Trace.trace(Activator.PLUGIN_ID, "Unrecognized authentication scheme."); //$NON-NLS-1$
		}

		if (credentials != null) {
			cachedCredentials.put(authscope, credentials);
		}

		return credentials;
	}

	public void clear() {
		this.cachedCredentials.clear();
	}
}
