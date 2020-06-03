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
 * Exception thrown during calls to
 * {@link ISharedObject#init(ISharedObjectConfig)}
 * 
 * @see ISharedObject#init(ISharedObjectConfig)
 */
public class SharedObjectInitException extends ECFException {
	private static final long serialVersionUID = 3617579318620862771L;

	public SharedObjectInitException() {
		super();
	}

	public SharedObjectInitException(IStatus status) {
		super(status);
	}
	public SharedObjectInitException(String arg0) {
		super(arg0);
	}

	public SharedObjectInitException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public SharedObjectInitException(Throwable cause) {
		super(cause);
	}
}