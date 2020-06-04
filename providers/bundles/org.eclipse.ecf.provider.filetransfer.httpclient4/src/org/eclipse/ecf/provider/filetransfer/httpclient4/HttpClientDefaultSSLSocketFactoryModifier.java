/****************************************************************************
 * Copyright (c) 2004, 2009 Composent, Inc., IBM and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *  Composent, Inc. - initial API and implementation
 *  Henrich Kraemer - bug 263869, testHttpsReceiveFile fails using HTTP proxy
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.provider.filetransfer.httpclient4;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import org.eclipse.ecf.filetransfer.events.socketfactory.INonconnectedSocketFactory;
import org.eclipse.ecf.internal.provider.filetransfer.httpclient4.ISSLSocketFactoryModifier;

public class HttpClientDefaultSSLSocketFactoryModifier implements ISSLSocketFactoryModifier, INonconnectedSocketFactory {
	public static final String DEFAULT_SSL_PROTOCOL = "https.protocols"; //$NON-NLS-1$

	private SSLContext sslContext = null;

	private String defaultProtocolNames = System.getProperty(DEFAULT_SSL_PROTOCOL);

	public HttpClientDefaultSSLSocketFactoryModifier() {
		// empty
	}

	public synchronized SSLSocketFactory getSSLSocketFactory() throws IOException {
		if (null == sslContext) {
			try {
				sslContext = getSSLContext(defaultProtocolNames);
			} catch (Exception e) {
				IOException ioe = new IOException();
				ioe.initCause(e);
				throw ioe;
			}
		}
		return (sslContext == null) ? (SSLSocketFactory) SSLSocketFactory.getDefault() : sslContext.getSocketFactory();
	}

	public synchronized SSLContext getSSLContext(String protocols) {
		SSLContext resultContext = null;
		if (protocols != null) {

			String[] httpsProtocols = protocols.split(",");
			// trim to make sure
			for (int i = 0; i < httpsProtocols.length; i++)
				httpsProtocols[i] = httpsProtocols[i].trim();
			// Now put into defaultProtocolsList in order of jreProtocols
			List<String> splitProtocolsList = Arrays.asList(httpsProtocols);
			List<String> defaultProtocolsList = new ArrayList();
			for (int i = 0; i < jreProtocols.length; i++)
				if (splitProtocolsList.contains(jreProtocols[i]))
					defaultProtocolsList.add(jreProtocols[i]);
			// In order of jre protocols, attempt to create and init SSLContext
			for (String protocol : defaultProtocolsList) {
				try {
					resultContext = SSLContext.getInstance(protocol);
					resultContext.init(null, new TrustManager[] {new HttpClientSslTrustManager()}, null);
					break;
				} catch (Exception e) {
					// just continue to look for SSLContexts with the next
					// protocolName
				}
			}
		}
		return resultContext;
	}

	public Socket createSocket() throws IOException {
		return getSSLSocketFactory().createSocket();
	}

	public void dispose() {
		// empty
	}

	public INonconnectedSocketFactory getNonconnnectedSocketFactory() {
		return this;
	}

	private static final String[] jreProtocols = new String[] {"TLSv1.2", "TLSv1.1", "TLSv1", "SSLv3"};

}
