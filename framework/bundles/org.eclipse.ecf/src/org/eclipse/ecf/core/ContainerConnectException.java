/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.util.ECFException;

/**
 * Exception class to be thrown upon connection failure.
 * 
 * @see IContainer#connect(org.eclipse.ecf.core.identity.ID,
 *      org.eclipse.ecf.core.security.IConnectContext)
 * 
 */
public class ContainerConnectException extends ECFException {
	private static final long serialVersionUID = 4078658849424746859L;

	public ContainerConnectException() {
		super();
	}

	public ContainerConnectException(String message) {
		super(message);
	}

	public ContainerConnectException(Throwable cause) {
		super(cause);
	}

	public ContainerConnectException(String message, Throwable cause) {
		super(message, cause);
	}

	public ContainerConnectException(IStatus status) {
		super(status);
	}
}