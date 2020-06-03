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
 * Exception thrown upon shared object add to container
 * 
 * @see ISharedObjectManager#addSharedObject(org.eclipse.ecf.core.identity.ID,
 *      ISharedObject, java.util.Map)
 */
public class SharedObjectAddException extends ECFException {
	private static final long serialVersionUID = 3257853198755705913L;

	public SharedObjectAddException(IStatus status) {
		super(status);
	}
	
	public SharedObjectAddException() {
		super();
	}

	public SharedObjectAddException(String arg0) {
		super(arg0);
	}

	public SharedObjectAddException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public SharedObjectAddException(Throwable cause) {
		super(cause);
	}
}