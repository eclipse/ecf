/*******************************************************************************
* Copyright (c) 2019 Yatta Solutions and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Yatta Solutions - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.internal.provider.filetransfer.httpclient45.win32;

import com.sun.jna.platform.win32.Sspi;
import java.util.Map;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Lookup;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.auth.DigestSchemeFactory;
import org.apache.http.impl.auth.KerberosSchemeFactory;
import org.apache.http.impl.auth.win.WindowsCredentialsProvider;
import org.apache.http.impl.auth.win.WindowsNTLMSchemeFactory;
import org.apache.http.impl.auth.win.WindowsNegotiateSchemeFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.ecf.internal.provider.filetransfer.httpclient45.HttpClientModifierAdapter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component
public class Win32HttpClientConfigurationModifier extends HttpClientModifierAdapter {

	public static final String ID = "org.eclipse.ecf.provider.filetransfer.httpclient45.win32"; //$NON-NLS-1$

	public static final String SERVICE_PRINCIPAL_NAME_ATTRIBUTE = "servicePrincipal"; //$NON-NLS-1$

	public static final String SERVICE_PRINCIPAL_NAME_PROPERTY = ID + "." + SERVICE_PRINCIPAL_NAME_ATTRIBUTE; //$NON-NLS-1$

	private static Boolean winAuthAvailable;

	private String servicePrincipalName;

	public static boolean isWinAuthAvailable() {
		if (winAuthAvailable == null) {
			// from org.apache.http.impl.client.WinHttpClients.isWinAuthAvailable()
			try {
				winAuthAvailable = Sspi.MAX_TOKEN_SIZE > 0;
			} catch (Exception ignore) { // Likely ClassNotFound
				winAuthAvailable = false;
			}
		}
		return winAuthAvailable;
	}

	@Override
	public HttpClientBuilder modifyClient(HttpClientBuilder builder) {
		if (!isWinAuthAvailable()) {
			return builder;
		}
		HttpClientBuilder winBuilder = builder == null ? HttpClientBuilder.create() : builder;
		Lookup<AuthSchemeProvider> authSchemeRegistry = createAuthSchemeRegistry();
		return winBuilder.setDefaultAuthSchemeRegistry(authSchemeRegistry);
	}

	@Override
	@SuppressWarnings("restriction")
	public CredentialsProvider modifyCredentialsProvider(CredentialsProvider credentialsProvider) {
		if (credentialsProvider == null || !isWinAuthAvailable() || credentialsProvider instanceof WindowsCredentialsProvider) {
			return credentialsProvider;
		}

		CredentialsProvider winCredentialsProvider = new WindowsCredentialsProvider(credentialsProvider);
		return winCredentialsProvider;
	}

	@Override
	public HttpClientContext modifyContext(HttpClientContext context) {
		Lookup<AuthSchemeProvider> authSchemeRegistry = context.getAuthSchemeRegistry();
		if (authSchemeRegistry == null) {
			authSchemeRegistry = createAuthSchemeRegistry();
		} else {
			authSchemeRegistry = modifyAuthSchemeRegistry(authSchemeRegistry);
		}
		context.setAuthSchemeRegistry(authSchemeRegistry);
		return context;
	}

	public void setServicePrincipalName(String servicePrincipalName) {
		this.servicePrincipalName = servicePrincipalName;
	}

	public String getServicePrincipalName() {
		return servicePrincipalName;
	}

	private Lookup<AuthSchemeProvider> createAuthSchemeRegistry() {
		Registry<AuthSchemeProvider> authSchemeRegistry = setWinAuthSchemes(RegistryBuilder.<AuthSchemeProvider> create().register(AuthSchemes.BASIC, new BasicSchemeFactory()).register(AuthSchemes.DIGEST, new DigestSchemeFactory()).register(AuthSchemes.KERBEROS, new KerberosSchemeFactory())).build();
		return authSchemeRegistry;
	}

	private Lookup<AuthSchemeProvider> modifyAuthSchemeRegistry(Lookup<AuthSchemeProvider> authSchemeRegistry) {
		RegistryBuilder<AuthSchemeProvider> builder = RegistryBuilder.create();
		for (String scheme : new String[] {AuthSchemes.BASIC, AuthSchemes.DIGEST, AuthSchemes.KERBEROS}) {
			AuthSchemeProvider provider = authSchemeRegistry.lookup(scheme);
			if (provider != null) {
				builder.register(scheme, provider);
			}
		}
		if (authSchemeRegistry.lookup(AuthSchemes.KERBEROS) == null) {
			builder.register(AuthSchemes.KERBEROS, new KerberosSchemeFactory());
		}
		setWinAuthSchemes(builder);
		return builder.build();
	}

	@SuppressWarnings("restriction")
	private RegistryBuilder<AuthSchemeProvider> setWinAuthSchemes(RegistryBuilder<AuthSchemeProvider> builder) {
		return builder.register(AuthSchemes.NTLM, new WindowsNTLMSchemeFactory(servicePrincipalName)).register(AuthSchemes.SPNEGO, new WindowsNegotiateSchemeFactory(servicePrincipalName));
	}

	@Activate
	public synchronized void activate(BundleContext context, Map<?, ?> properties) {
		this.servicePrincipalName = getServicePrincipalName(properties);
	}

	private String getServicePrincipalName(Map<?, ?> properties) {
		Object servicePrincipalValue = properties.get(SERVICE_PRINCIPAL_NAME_ATTRIBUTE);
		if (servicePrincipalValue != null) {
			return servicePrincipalValue.toString();
		}
		Bundle bundle = FrameworkUtil.getBundle(this.getClass());
		if (bundle != null) {
			return bundle.getBundleContext().getProperty(SERVICE_PRINCIPAL_NAME_PROPERTY);
		}
		return System.getProperty(SERVICE_PRINCIPAL_NAME_PROPERTY);
	}

	@Override
	public Builder modifyRequestConfig(Builder config, HttpClientContext context, Map<?, ?> options) {
		// TODO Auto-generated method stub
		return null;
	}

}
