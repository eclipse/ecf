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
 * Outgoing file transfer exception
 * 
 */
public class SendFileTransferException extends ECFException {

	private static final long serialVersionUID = -3752377147967128446L;

	public SendFileTransferException(IStatus status) {
		super(status);
	}

	public SendFileTransferException() {
		// null constructor
	}

	public SendFileTransferException(String message) {
		super(message);
	}

	public SendFileTransferException(Throwable cause) {
		super(cause);
	}

	public SendFileTransferException(String message, Throwable cause) {
		super(message, cause);
	}

}
