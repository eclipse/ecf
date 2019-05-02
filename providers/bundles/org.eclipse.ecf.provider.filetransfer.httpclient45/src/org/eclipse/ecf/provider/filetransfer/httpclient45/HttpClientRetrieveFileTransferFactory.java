/****************************************************************************
 * Copyright (c) 2019 IBM, Composent Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *    Thomas Joiner - HttpClient 4 implementation
 *    Yatta Solutions - HttpClient 4.5 implementation
 *****************************************************************************/
package org.eclipse.ecf.provider.filetransfer.httpclient45;

import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransfer;
import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransferFactory;
import org.eclipse.ecf.internal.provider.filetransfer.httpclient45.Activator;

public class HttpClientRetrieveFileTransferFactory implements IRetrieveFileTransferFactory {

	@Override
	public IRetrieveFileTransfer newInstance() {
		return new HttpClientRetrieveFileTransfer(Activator.getDefault().getRetrieveHttpClient());
	}
}
