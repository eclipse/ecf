/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.internal.provider.filetransfer.httpclient;

import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.eclipse.ecf.core.util.Proxy;

/**
 * Internal interface to allow for use of httpclient.ssl provided socket factory
 */
public interface ISSLSocketFactoryModifier {

	public ProtocolSocketFactory getProtocolSocketFactoryForProxy(Proxy proxy);

	public void dispose();

}
