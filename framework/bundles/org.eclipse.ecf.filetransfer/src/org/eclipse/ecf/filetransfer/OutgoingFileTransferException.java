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
public class OutgoingFileTransferException extends ECFException {

	private static final long serialVersionUID = -3752377147967128446L;

	public OutgoingFileTransferException(IStatus status) {
		super(status);
	}
	
	public OutgoingFileTransferException() {
	}

	public OutgoingFileTransferException(String message) {
		super(message);
	}

	public OutgoingFileTransferException(Throwable cause) {
		super(cause);
	}

	public OutgoingFileTransferException(String message, Throwable cause) {
		super(message, cause);
	}

}
