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
package org.eclipse.ecf.core.sharedobject.util;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.util.ECFException;

public class QueueException extends ECFException {
	private static final long serialVersionUID = 3691039863709118774L;

	IQueue theQueue = null;

	public QueueException(IStatus status) {
		super(status);
	}
	public QueueException() {
		super();
	}

	public QueueException(IQueue queue) {
		theQueue = queue;
	}

	public QueueException(String message) {
		super(message);
	}

	public QueueException(String message, Throwable cause) {
		super(message, cause);
	}

	public QueueException(Throwable cause) {
		super(cause);
	}

	public IQueue getQueue() {
		return theQueue;
	}
}