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

package org.eclipse.ecf.internal.provider.filetransfer.httpclient4;

import java.io.IOException;
import javax.net.ssl.SSLSocketFactory;
import org.eclipse.ecf.filetransfer.events.socketfactory.INonconnectedSocketFactory;

/**
 * Internal interface to allow for use of httpclient.ssl provided socket factory
 */
public interface ISSLSocketFactoryModifier {

	public SSLSocketFactory getSSLSocketFactory() throws IOException;

	public INonconnectedSocketFactory getNonconnnectedSocketFactory();

	public void dispose();

}
