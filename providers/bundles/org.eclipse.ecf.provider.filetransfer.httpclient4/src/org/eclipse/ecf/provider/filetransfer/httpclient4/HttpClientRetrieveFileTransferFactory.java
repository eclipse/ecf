/****************************************************************************
 * Copyright (c) 2007 IBM, Composent Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *    Thomas Joiner - HttpClient 4 implementation
 *****************************************************************************/
package org.eclipse.ecf.provider.filetransfer.httpclient4;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransfer;
import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransferFactory;

public class HttpClientRetrieveFileTransferFactory implements IRetrieveFileTransferFactory {

	public IRetrieveFileTransfer newInstance() {
		return new HttpClientRetrieveFileTransfer(new DefaultHttpClient(new SingleClientConnManager()));
	}
}
