/****************************************************************************
 * Copyright (c) 2024 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.core.security;

import java.security.*;
import java.util.Optional;
import javax.net.ssl.SSLContext;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @since 3.12
 */
public class ECFSSLContextFactory implements SSLContextFactory {

	private final ServiceTracker<Provider, Provider> providerTracker;
	private final String defaultProtocol;
	private final String defaultProviderName;

	public ECFSSLContextFactory(BundleContext context, String defaultProtocol) {
		this(context, defaultProtocol, null);
	}

	public ECFSSLContextFactory(BundleContext context, String defaultProtocol, String defaultProviderName) {
		this.defaultProtocol = defaultProtocol;
		this.defaultProviderName = defaultProviderName;
		this.providerTracker = new ServiceTracker<Provider, Provider>(context, Provider.class, null);
		this.providerTracker.open();
	}

	@Override
	public SSLContext getDefault() throws NoSuchAlgorithmException, NoSuchProviderException {
		return getInstance0(this.defaultProtocol, this.defaultProviderName);
	}

	protected SSLContext getInstance0(String protocol, String providerName) throws NoSuchAlgorithmException, NoSuchProviderException {
		if (protocol == null) {
			return SSLContext.getDefault();
		}
		Provider provider = findProvider(providerName);
		if (provider == null)
			throw new NoSuchProviderException("No provider registered named '" + providerName + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		return createSSLContext(protocol, provider);
	}

	protected SSLContext createSSLContext(String protocol, Provider provider) throws NoSuchAlgorithmException {
		return SSLContext.getInstance(protocol, provider);
	}

	@Override
	public SSLContext getInstance(String protocol) throws NoSuchAlgorithmException, NoSuchProviderException {
		return getInstance0(protocol, this.defaultProviderName);
	}

	public void close() {
		this.providerTracker.close();
	}

	protected Provider findProvider(String providerName) {
		if (providerName == null) {
			return this.providerTracker.getService();
		}
		Optional<Provider> optResult = this.providerTracker.getTracked().values().stream().filter(p ->
		// test that providerName is equal to Provider.getName()
		providerName.equals(p.getName())).findFirst();
		// If there are matching Providers, use first (highest priority from sorted map) and use to create SSLContext.  
		// If none, then throw
		return optResult.isPresent() ? optResult.get() : null;
	}

	@Override
	public SSLContext getInstance(String protocol, String providerName) throws NoSuchAlgorithmException, NoSuchProviderException {
		return getInstance0(protocol, providerName);
	}

}
