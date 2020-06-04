/****************************************************************************
 * Copyright (c) 2008, 2009 Composent, Inc., IBM and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *    Henrich Kraemer - bug 263869, testHttpsReceiveFile fails using HTTP proxy
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.internal.provider.filetransfer.httpclient4.ssl;

import java.io.IOException;
import java.net.Socket;
import javax.net.ssl.SSLSocketFactory;
import org.eclipse.ecf.filetransfer.events.socketfactory.INonconnectedSocketFactory;
import org.eclipse.ecf.internal.provider.filetransfer.httpclient4.Activator;
import org.eclipse.ecf.internal.provider.filetransfer.httpclient4.ISSLSocketFactoryModifier;

public class SSLSocketFactoryModifier implements ISSLSocketFactoryModifier, INonconnectedSocketFactory {

	public void dispose() {
		// nothing to do
	}

	public SSLSocketFactory getSSLSocketFactory() throws IOException {
		final SSLSocketFactory factory = Activator.getDefault().getSSLSocketFactory();
		if (factory == null)
			throw new IOException("Cannot get socket factory"); //$NON-NLS-1$
		return factory;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.internal.provider.filetransfer.httpclient.ISSLSocketFactoryModifier#getNonconnnectedSocketFactory()
	 */
	public INonconnectedSocketFactory getNonconnnectedSocketFactory() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.events.socketfactory.INonconnectedSocketFactory#createSocket()
	 */
	public Socket createSocket() throws IOException {
		final SSLSocketFactory factory = getSSLSocketFactory();
		return factory.createSocket();
	}

}
