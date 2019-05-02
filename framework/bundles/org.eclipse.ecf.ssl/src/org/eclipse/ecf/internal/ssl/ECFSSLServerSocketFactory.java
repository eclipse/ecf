/****************************************************************************
 * Copyright (c) 2012 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.internal.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

public class ECFSSLServerSocketFactory extends SSLServerSocketFactory {

	public static final String DEFAULT_SSL_PROTOCOL = "https.protocols"; //$NON-NLS-1$

	private String defaultProtocolNames = System.getProperty(DEFAULT_SSL_PROTOCOL);

	private SSLContext sslContext = null;

	private SSLServerSocketFactory getSSLServerSocketFactory() throws IOException {
		if (null == sslContext) {
			try {
				sslContext = getSSLContext(defaultProtocolNames);
			} catch (Exception e) {
				IOException ioe = new IOException();
				ioe.initCause(e);
				throw ioe;
			}
		}
		return (sslContext == null) ? (SSLServerSocketFactory) SSLServerSocketFactory.getDefault()
				: sslContext.getServerSocketFactory();
	}

	public SSLContext getSSLContext(String protocols) {
		return SSLContextHelper.getSSLContext(protocols);
	}

	public String[] getDefaultCipherSuites() {
		try {
			return getSSLServerSocketFactory().getDefaultCipherSuites();
		} catch (IOException e) {
			return new String[] {};
		}
	}

	public String[] getSupportedCipherSuites() {
		try {
			return getSSLServerSocketFactory().getSupportedCipherSuites();
		} catch (IOException e) {
			return new String[] {};
		}
	}

	public ServerSocket createServerSocket(int arg0) throws IOException {
		return getSSLServerSocketFactory().createServerSocket(arg0);
	}

	public ServerSocket createServerSocket(int arg0, int arg1) throws IOException {
		return getSSLServerSocketFactory().createServerSocket(arg0, arg1);
	}

	public ServerSocket createServerSocket(int arg0, int arg1, InetAddress arg2) throws IOException {
		return getSSLServerSocketFactory().createServerSocket(arg0, arg1, arg2);
	}

}
