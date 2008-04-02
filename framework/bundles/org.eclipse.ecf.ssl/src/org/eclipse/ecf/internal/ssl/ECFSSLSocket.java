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
import java.net.Socket;

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

public class ECFSSLSocket extends SSLSocket {
	
	private SSLSocket sslSocket;
	
	public ECFSSLSocket(Socket socket) {
		this.sslSocket = (SSLSocket) socket;
	}
	
	public void addHandshakeCompletedListener(HandshakeCompletedListener arg0) {
		sslSocket.addHandshakeCompletedListener(arg0);
	}

	public boolean getEnableSessionCreation() {
		return sslSocket.getEnableSessionCreation();
	}

	public String[] getEnabledCipherSuites() {
		return sslSocket.getEnabledCipherSuites();
	}

	public String[] getEnabledProtocols() {
		return sslSocket.getEnabledProtocols();
	}

	public boolean getNeedClientAuth() {
		return sslSocket.getNeedClientAuth();
	}

	public SSLSession getSession() {
		return sslSocket.getSession();
	}

	public String[] getSupportedCipherSuites() {
		return sslSocket.getSupportedCipherSuites();
	}

	public String[] getSupportedProtocols() {
		return sslSocket.getSupportedProtocols();
	}

	public boolean getUseClientMode() {
		return sslSocket.getUseClientMode();
	}

	public boolean getWantClientAuth() {
		return sslSocket.getWantClientAuth();
	}

	public void removeHandshakeCompletedListener(HandshakeCompletedListener arg0) {
		sslSocket.removeHandshakeCompletedListener(arg0);
	}

	public void setEnableSessionCreation(boolean arg0) {
		sslSocket.setEnableSessionCreation(arg0);
	}

	public void setEnabledCipherSuites(String[] arg0) {
		sslSocket.setEnabledCipherSuites(arg0);
	}

	public void setEnabledProtocols(String[] arg0) {
		sslSocket.setEnabledProtocols(arg0);
	}

	public void setNeedClientAuth(boolean arg0) {
		sslSocket.setNeedClientAuth(arg0);
	}

	public void setUseClientMode(boolean arg0) {
		sslSocket.setUseClientMode(arg0);
	}

	public void setWantClientAuth(boolean arg0) {
		sslSocket.setWantClientAuth(arg0);
	}

	public void startHandshake() throws IOException {
		sslSocket.startHandshake();		
		// could catch the CertificateException here...
	}

}