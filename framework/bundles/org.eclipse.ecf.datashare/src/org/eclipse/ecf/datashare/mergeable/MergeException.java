/****************************************************************************
 * Copyright (c) 2004 Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.datashare.mergeable;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.util.ECFException;

public class MergeException extends ECFException {
	public MergeException() {
		super();
	}

	public MergeException(IStatus status) {
		super(status);
	}
	public MergeException(String message, Throwable cause) {
		super(message, cause);
	}

	public MergeException(String message) {
		super(message);
	}

	public MergeException(Throwable cause) {
		super(cause);
	}

	private static final long serialVersionUID = -4834493736186063964L;
}
