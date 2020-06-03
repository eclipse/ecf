/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.provider.comm;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.util.ECFException;

/**
 * Exception class for connection creation exceptions
 * 
 */
public class ConnectionCreateException extends ECFException {
	private static final long serialVersionUID = 3904958651231058229L;

	public ConnectionCreateException(IStatus status) {
		super(status);
	}

	public ConnectionCreateException() {
		super();
	}

	public ConnectionCreateException(String message) {
		super(message);
	}

	public ConnectionCreateException(Throwable cause) {
		super(cause);
	}

	public ConnectionCreateException(String message, Throwable cause) {
		super(message, cause);
	}
}