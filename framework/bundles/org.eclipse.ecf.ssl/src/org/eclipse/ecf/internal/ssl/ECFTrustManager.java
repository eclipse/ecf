/*******************************************************************************
 * Copyright (c)2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.internal.ssl;

import java.io.IOException;
import java.security.cert.*;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import org.eclipse.osgi.service.security.TrustEngine;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class ECFTrustManager implements X509TrustManager, BundleActivator {

	private static volatile BundleContext context;
	private volatile ServiceTracker trustEngineTracker = null;

	public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
		// verify the cert chain
		verify(certs, authType);

		final TrustEngine[] engines = getTrustEngines();
		Certificate foundCert = null;
		for (int i = 0; i < engines.length; i++) {
			try {
				foundCert = engines[i].findTrustAnchor(certs);
				if (null != foundCert)
					return; // cert chain is trust
			} catch (final IOException e) {
				final CertificateException ce = new ECFCertificateException("Error occurs when finding trust anchor in the cert chain", certs, authType); //$NON-NLS-1$
				ce.initCause(ce);
				throw ce;
			}
		}
		if (null == foundCert)
			throw new ECFCertificateException("Valid cert chain, but no trust certificate found!", certs, authType); //$NON-NLS-1$
	}

	private void verify(X509Certificate[] certs, String authType) throws CertificateException {
		final int len = certs.length;
		for (int i = 0; i < len; i++) {
			final X509Certificate currentX509Cert = certs[i];
			try {
				if (i == len - 1) {
					if (currentX509Cert.getSubjectDN().equals(currentX509Cert.getIssuerDN()))
						currentX509Cert.verify(currentX509Cert.getPublicKey());
				} else {
					final X509Certificate nextX509Cert = certs[i + 1];
					currentX509Cert.verify(nextX509Cert.getPublicKey());
				}
			} catch (final Exception e) {
				final CertificateException ce = new ECFCertificateException("Certificate chain is not valid", certs, authType); //$NON-NLS-1$
				ce.initCause(e);
				throw ce;
			}
		}
	}

	/**
	 * @throws CertificateException not actually thrown by method, since checkClientTrusted is unsupported. 
	 */
	public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		// only for client authentication
		throw new UnsupportedOperationException("Not implemented yet"); //$NON-NLS-1$
	}

	public X509Certificate[] getAcceptedIssuers() {
		// only for client authentication
		return null;
	}

	public void start(BundleContext context1) throws Exception {
		ECFTrustManager.context = context1;
		context1.registerService(SSLSocketFactory.class.getName(), new ECFSSLSocketFactory(), null);
	}

	public void stop(BundleContext context1) throws Exception {
		if (trustEngineTracker != null) {
			trustEngineTracker.close();
			trustEngineTracker = null;
		}
		ECFTrustManager.context = null;
	}

	private TrustEngine[] getTrustEngines() {
		if (trustEngineTracker == null) {
			trustEngineTracker = new ServiceTracker(context, TrustEngine.class.getName(), null);
			trustEngineTracker.open();
		}
		final Object objs[] = trustEngineTracker.getServices();
		final TrustEngine[] result = new TrustEngine[objs.length];
		System.arraycopy(objs, 0, result, 0, objs.length);
		return result;
	}
}