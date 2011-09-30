/****************************************************************************
 * Copyright (c) 2007, 2011 IBM, Composent Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *    Henrich Kraemer - Bug 297742 - [transport] Investigate how to maintain HTTP session 
 *****************************************************************************/
package org.eclipse.ecf.provider.filetransfer.httpclient;

import org.apache.commons.httpclient.HttpClient;
import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransfer;
import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransferFactory;

public class HttpClientRetrieveFileTransferFactory implements IRetrieveFileTransferFactory {

	public IRetrieveFileTransfer newInstance() {
		return new HttpClientRetrieveFileTransfer(new HttpClient());
	}

}
