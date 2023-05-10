/****************************************************************************
 * Copyright (c)2008 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.ssl;

import java.io.IOException;
import java.security.cert.*;
import javax.net.ssl.*;
import org.eclipse.osgi.service.security.TrustEngine;
import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;

public class ECFTrustManager implements X509TrustManager, BundleActivator {

	public static final String SORT_CERTS_EXPERIMENTAL_FLAG = "org.eclipse.ecf.internal.ssl.ECFTrustManager.experimental.sortCerts";

	private static volatile BundleContext context;
	private volatile ServiceTracker trustEngineTracker = null;
	private ServiceRegistration socketFactoryRegistration;
	private ServiceRegistration serverSocketFactoryRegistration;

	public void checkServerTrusted(X509Certificate[] certs, String authType)
			throws CertificateException {

		if (Boolean.getBoolean(SORT_CERTS_EXPERIMENTAL_FLAG)) {
			certs = CertificateChainSorter.sortCertificates(certs);
		}

		// verify the cert chain
		verify(certs, authType);

		final TrustEngine[] engines = getTrustEngines();
		Certificate foundCert = null;
		for (TrustEngine engine : engines) {
			try {
				foundCert = engine.findTrustAnchor(certs);
				if (null != foundCert)
					return; // cert chain is trust
			}catch (final IOException e) {
				final CertificateException ce = new ECFCertificateException(
						"Error occurs when finding trust anchor in the cert chain", certs, authType); //$NON-NLS-1$
				ce.initCause(ce);
				throw ce;
			}
		}
		if (null == foundCert)
			throw new ECFCertificateException(
					"Valid cert chain, but no trust certificate found!", certs, authType); //$NON-NLS-1$
	}

	private void verify(X509Certificate[] certs, String authType)
			throws CertificateException {
		final int len = certs.length;
		for (int i = 0; i < len; i++) {
			final X509Certificate currentX509Cert = certs[i];
			try {
				if (i == len - 1) {
					if (currentX509Cert.getSubjectDN().equals(
							currentX509Cert.getIssuerDN()))
						currentX509Cert.verify(currentX509Cert.getPublicKey());
				} else {
					final X509Certificate nextX509Cert = certs[i + 1];
					currentX509Cert.verify(nextX509Cert.getPublicKey());
				}
			} catch (final Exception e) {
				final CertificateException ce = new ECFCertificateException(
						"Certificate chain is not valid", certs, authType); //$NON-NLS-1$
				ce.initCause(e);
				throw ce;
			}
		}
	}

	/**
	 * @throws CertificateException
	 *             not actually thrown by method, since checkClientTrusted is
	 *             unsupported.
	 */
	public void checkClientTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
		// only for client authentication
		throw new UnsupportedOperationException("Not implemented yet"); //$NON-NLS-1$
	}

	public X509Certificate[] getAcceptedIssuers() {
		// only for client authentication
		return null;
	}

	public void start(BundleContext context1) throws Exception {
		ECFTrustManager.context = context1;
		socketFactoryRegistration = context1.registerService(
				SSLSocketFactory.class.getName(), new ECFSSLSocketFactory(),
				null);
		serverSocketFactoryRegistration = context1.registerService(
				SSLServerSocketFactory.class.getName(),
				new ECFSSLServerSocketFactory(), null);
	}

	public void stop(BundleContext context1) throws Exception {
		if (socketFactoryRegistration != null) {
			socketFactoryRegistration.unregister();
			socketFactoryRegistration = null;
		}
		if (serverSocketFactoryRegistration != null) {
			serverSocketFactoryRegistration.unregister();
			serverSocketFactoryRegistration = null;
		}
		if (trustEngineTracker != null) {
			trustEngineTracker.close();
			trustEngineTracker = null;
		}
		ECFTrustManager.context = null;
	}

	private TrustEngine[] getTrustEngines() {
		if (trustEngineTracker == null) {
			trustEngineTracker = new ServiceTracker(context,
					TrustEngine.class.getName(), null);
			trustEngineTracker.open();
		}
		final Object objs[] = trustEngineTracker.getServices();
		final TrustEngine[] result = new TrustEngine[objs.length];
		System.arraycopy(objs, 0, result, 0, objs.length);
		return result;
	}
}