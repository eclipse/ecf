/****************************************************************************
* Copyright (c) 2004 Composent, Inc. and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Composent, Inc. - initial API and implementation
*****************************************************************************/

package org.eclipse.ecf.internal.provider.filetransfer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class SslProtocolSocketFactory implements ProtocolSocketFactory {

	private SSLContext sslContext;

	public SslProtocolSocketFactory() {
	}

	private SSLContext getSslContext() {
		if (sslContext == null) {
			try {
				sslContext = SSLContext.getInstance("SSL");
				sslContext.init(null, new TrustManager[] { new HttpClientSslTrustManager() }, null);
			} catch (Exception e) {
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR,Activator.PLUGIN_ID,1111,"SslProtocolSocketFactory",e));
			}
		}
		return sslContext;
	}

	public Socket createSocket(String remoteHost, int remotePort) throws IOException, UnknownHostException {
		return getSslContext().getSocketFactory().createSocket(remoteHost, remotePort);
	}

	public Socket createSocket(String remoteHost, int remotePort, InetAddress clientHost, int clientPort)
			throws IOException, UnknownHostException {
		return getSslContext().getSocketFactory().createSocket(remoteHost, remotePort, clientHost, clientPort);
	}

	public Socket createSocket(String remoteHost, int remotePort, InetAddress clientHost, int clientPort,
			HttpConnectionParams params) throws IOException, UnknownHostException, ConnectTimeoutException {
		if (params == null || params.getConnectionTimeout() == 0)
			return getSslContext().getSocketFactory().createSocket(remoteHost, remotePort, clientHost, clientPort);

		// XXX setup proxy here if we have a proxy
		
		Socket socket = getSslContext().getSocketFactory().createSocket();
		socket.bind(new InetSocketAddress(clientHost, clientPort));
		socket.connect(new InetSocketAddress(remoteHost, remotePort), params.getConnectionTimeout());
		return socket;
	}

}
