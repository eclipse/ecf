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
package org.eclipse.ecf.core.sharedobject;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.util.ECFException;

/**
 * Exception thrown upon
 * {@link ISharedObjectManager#disconnectSharedObjects(ISharedObjectConnector)}
 * 
 * @see ISharedObjectManager#disconnectSharedObjects(ISharedObjectConnector)
 */
public class SharedObjectDisconnectException extends ECFException {
	private static final long serialVersionUID = 3258689922876586289L;

	public SharedObjectDisconnectException() {
		super();
	}

	public SharedObjectDisconnectException(IStatus status) {
		super(status);
	}
	public SharedObjectDisconnectException(String arg0) {
		super(arg0);
	}

	public SharedObjectDisconnectException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public SharedObjectDisconnectException(Throwable cause) {
		super(cause);
	}
}