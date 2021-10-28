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
package org.eclipse.ecf.provider.filetransfer.httpclient5.win32;

import java.util.Map;

import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.config.Lookup;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.eclipse.ecf.internal.provider.filetransfer.httpclient5.HttpClientModifierAdapter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component
public class Win32HttpClientConfigurationModifier extends HttpClientModifierAdapter {

	public static final String ID = "org.eclipse.ecf.provider.filetransfer.httpclient5.win32"; //$NON-NLS-1$

	public static final String SERVICE_PRINCIPAL_NAME_ATTRIBUTE = "servicePrincipal"; //$NON-NLS-1$

	public static final String SERVICE_PRINCIPAL_NAME_PROPERTY = ID + "." + SERVICE_PRINCIPAL_NAME_ATTRIBUTE; //$NON-NLS-1$

	private String servicePrincipalName;

	@Override
	public HttpClientBuilder modifyClient(HttpClientBuilder builder) {
		HttpClientBuilder winBuilder = builder == null ? HttpClientBuilder.create() : builder;
		Lookup<AuthSchemeProvider> authSchemeRegistry = createAuthSchemeRegistry();
		return winBuilder.setDefaultAuthSchemeRegistry(authSchemeRegistry);
	}

	@Override
	@SuppressWarnings("restriction")
	public CredentialsProvider modifyCredentialsProvider(CredentialsProvider credentialsProvider) {
		if (credentialsProvider == null || credentialsProvider instanceof WindowsCredentialsProvider) {
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
