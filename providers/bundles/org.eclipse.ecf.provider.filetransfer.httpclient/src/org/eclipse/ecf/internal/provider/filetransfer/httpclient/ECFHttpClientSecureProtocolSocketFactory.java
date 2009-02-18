/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.internal.provider.filetransfer.httpclient;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.eclipse.ecf.filetransfer.events.socket.ISocketEventSource;
import org.eclipse.ecf.filetransfer.events.socket.ISocketListener;
import org.eclipse.ecf.filetransfer.events.socketfactory.INonconnectedSocketFactory;

public final class ECFHttpClientSecureProtocolSocketFactory extends ECFHttpClientProtocolSocketFactory implements SecureProtocolSocketFactory {

	private ISSLSocketFactoryModifier sslSocketFactoryModifier;

	public ECFHttpClientSecureProtocolSocketFactory(final ISSLSocketFactoryModifier sslSocketFactoryModifier, ISocketEventSource source, ISocketListener socketConnectListener) {
		super(new INonconnectedSocketFactory() {
			public Socket createSocket() throws IOException {
				return sslSocketFactoryModifier.getNonconnnectedSocketFactory().createSocket();
			}

		}, source, socketConnectListener);

		this.sslSocketFactoryModifier = sslSocketFactoryModifier;
	}

	public boolean equals(Object obj) {
		return ((obj != null) && obj.getClass().equals(ECFHttpClientSecureProtocolSocketFactory.class));
	}

	public int hashCode() {
		return ECFHttpClientSecureProtocolSocketFactory.class.hashCode();
	}

	public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
		// Socket over the tunnel need not be monitored or do they ?
		SSLSocketFactory sslSocketFactory = sslSocketFactoryModifier.getSSLSocketFactory();
		return sslSocketFactory.createSocket(socket, host, port, autoClose);
	}

}
