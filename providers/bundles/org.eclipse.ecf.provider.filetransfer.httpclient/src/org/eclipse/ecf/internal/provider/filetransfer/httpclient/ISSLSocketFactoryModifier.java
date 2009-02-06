/****************************************************************************
 * Copyright (c) 2008, 2009 Composent, Inc., IBM and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *    Henrich Kraemer - bug 263869, testHttpsReceiveFile fails using HTTP proxy
 *****************************************************************************/

package org.eclipse.ecf.internal.provider.filetransfer.httpclient;

import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;

/**
 * Internal interface to allow for use of httpclient.ssl provided socket factory
 */
public interface ISSLSocketFactoryModifier {

	public SecureProtocolSocketFactory getProtocolSocketFactory();

	public void dispose();

}
