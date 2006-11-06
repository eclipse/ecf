/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.filetransfer;

import org.eclipse.ecf.core.util.ECFException;

/**
 * Exception thrown upon incoming file transfer problem
 * 
 */
public class IncomingFileTransferException extends ECFException {

	private static final long serialVersionUID = 2438441801862623371L;

	public IncomingFileTransferException() {
	}

	public IncomingFileTransferException(String message) {
		super(message);
	}

	public IncomingFileTransferException(Throwable cause) {
		super(cause);
	}

	public IncomingFileTransferException(String message, Throwable cause) {
		super(message, cause);
	}

}
