/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
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

package org.eclipse.ecf.sync;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.internal.sync.Activator;

/**
 * Exception for serialization problems.
 * 
 * @since 2.1
 */
public class SerializationException extends ECFException {

	private static final long serialVersionUID = -8702959540799683251L;

	/**
	 * @param message
	 *            message associated with exception
	 */
	public SerializationException(String message) {
		this(message, null);
	}

	/**
	 * @param cause
	 *            the cause of the new exception
	 */
	public SerializationException(Throwable cause) {
		this(null, cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SerializationException(String message, Throwable cause) {
		this(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, ((message == null) ? "" : message), cause)); //$NON-NLS-1$
	}

	/**
	 * @param status
	 *            the status for th
	 */
	public SerializationException(IStatus status) {
		super(status);
	}

}
