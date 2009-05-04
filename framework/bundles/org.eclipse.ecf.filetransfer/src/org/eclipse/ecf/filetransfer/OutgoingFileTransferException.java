/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.filetransfer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.util.ECFException;

/**
 * Exception thrown upon outgoing file transfer problem
 * 
 */
public class OutgoingFileTransferException extends ECFException {

	private static final long serialVersionUID = 2438441801862623371L;

	private int errorCode = -1;

	public OutgoingFileTransferException(IStatus status) {
		super(status);
	}

	public OutgoingFileTransferException() {
		// null constructor
	}

	public OutgoingFileTransferException(int errorCode) {
		this();
		this.errorCode = errorCode;
	}

	public OutgoingFileTransferException(String message) {
		super(message);
	}

	public OutgoingFileTransferException(String message, int errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	public OutgoingFileTransferException(Throwable cause) {
		super(cause);
	}

	public OutgoingFileTransferException(Throwable cause, int errorCode) {
		super(cause);
		this.errorCode = errorCode;
	}

	public OutgoingFileTransferException(String message, Throwable cause) {
		super(message, cause);
	}

	public OutgoingFileTransferException(String message, Throwable cause, int errorCode) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}
}
