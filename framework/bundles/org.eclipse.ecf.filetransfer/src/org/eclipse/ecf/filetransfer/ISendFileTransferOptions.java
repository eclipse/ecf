/*******************************************************************************
* Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.filetransfer;

/**
 * @since 3.1
 */
public interface ISendFileTransferOptions {

	public static final String REQUEST_HEADERS = ISendFileTransferOptions.class.getName() + ".requestHeaders"; //$NON-NLS-1$

	public static final String CONNECT_TIMEOUT = ISendFileTransferOptions.class.getName() + ".connectTimeout"; //$NON-NLS-1$

	public static final String READ_TIMEOUT = ISendFileTransferOptions.class.getName() + ".readTimeout"; //$NON-NLS-1$
}
