/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
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

package org.eclipse.ecf.internal.provider.filetransfer.ssl;

import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.eclipse.ecf.internal.provider.filetransfer.IURLConnectionModifier;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 *
 */
public class ECFURLConnectionModifier implements IURLConnectionModifier {

	private BundleContext context;
	private ServiceTracker sslSocketFactoryTracker;

	public void init(BundleContext ctxt) {
		this.context = ctxt;
	}

	private SSLSocketFactory getSSLSocketFactory() {
		if (context == null)
			return null;
		if (sslSocketFactoryTracker == null) {
			sslSocketFactoryTracker = new ServiceTracker(this.context, SSLSocketFactory.class.getName(), null);
			sslSocketFactoryTracker.open();
		}
		return (SSLSocketFactory) sslSocketFactoryTracker.getService();
	}

	public void dispose() {
		this.context = null;
		if (sslSocketFactoryTracker != null) {
			sslSocketFactoryTracker.close();
			sslSocketFactoryTracker = null;
		}
	}

	public void setSocketFactoryForConnection(URLConnection urlConnection) {
		if (urlConnection instanceof HttpsURLConnection) {
			final HttpsURLConnection httpsURLConnection = (HttpsURLConnection) urlConnection;
			final SSLSocketFactory sslSocketFactory = getSSLSocketFactory();
			if (sslSocketFactory != null)
				httpsURLConnection.setSSLSocketFactory(sslSocketFactory);
		}
	}
}
