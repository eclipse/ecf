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
 * Exception thrown upon shared object create by {@link ISharedObjectManager}
 * 
 * @see ISharedObjectManager#createSharedObject(SharedObjectDescription)
 */
public class SharedObjectCreateException extends ECFException {
	private static final long serialVersionUID = 3546919195137815606L;

	public SharedObjectCreateException() {
		super();
	}

	public SharedObjectCreateException(IStatus status) {
		super(status);
	}
	public SharedObjectCreateException(String arg0) {
		super(arg0);
	}

	public SharedObjectCreateException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public SharedObjectCreateException(Throwable cause) {
		super(cause);
	}
}