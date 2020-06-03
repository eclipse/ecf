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

package org.eclipse.ecf.storage;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.util.ECFException;

/**
 *
 */
public class IDStoreException extends ECFException {

	private static final long serialVersionUID = 3886247422255119017L;

	public IDStoreException() {
	}

	/**
	 * @param status
	 */
	public IDStoreException(IStatus status) {
		super(status);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public IDStoreException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public IDStoreException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public IDStoreException(Throwable cause) {
		super(cause);
	}

}
