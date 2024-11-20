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
import java.util.Map;
import java.util.Optional;
import javax.net.ssl.SSLContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @since 3.12
 */
public class ECFSSLContextFactory implements SSLContextFactory {

	private final ServiceTracker<Provider, Provider> providerTracker;

	public ECFSSLContextFactory(BundleContext context) {
		this.providerTracker = new ServiceTracker<Provider, Provider>(context, Provider.class, null);
		this.providerTracker.open();
	}

	@Override
	public SSLContext getDefault() throws NoSuchAlgorithmException {
		return SSLContext.getDefault();
	}

	@Override
	public SSLContext getInstance(String protocol) throws NoSuchAlgorithmException, NoSuchProviderException {
		return getInstance(protocol, null);
	}

	@Override
	public SSLContext getInstance(String protocol, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
		if (protocol == null) {
			return null;
		}
		// Filter out Providers that do not have given protocol and (optionally) do not have given provider name
		Optional<Map.Entry<ServiceReference<Provider>, Provider>> optResult = this.providerTracker.getTracked().entrySet().stream().filter(entry ->
		// first test that protocol is equal to value of SSLContextFactory.PROTOCOL_PROPERTY_NAME
		entry.getKey().getProperty(SSLContextFactory.PROTOCOL_PROPERTY_NAME) != null &&
		// And test that either provder is null, or provider equals the Provider.getName() value
				provider == null || provider.equals(entry.getValue().getName())).findFirst();
		// If any remaining Providers, use first (highest priority from sorted map) and use to create SSLContext.  
		// If none, call SSLContext.getInstance(String,String) with given protocol and provider
		if (optResult.isPresent()) {
			return SSLContext.getInstance(protocol, optResult.get().getValue());
		} else if (provider != null) {
			return SSLContext.getInstance(protocol, provider);
		}
		return SSLContext.getInstance(protocol);
	}

	public void close() {
		this.providerTracker.close();
	}

}
