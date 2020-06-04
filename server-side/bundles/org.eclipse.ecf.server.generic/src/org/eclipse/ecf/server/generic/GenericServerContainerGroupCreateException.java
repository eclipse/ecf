/****************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.server.generic;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.util.ECFException;

/**
 * @since 4.0
 */
public class GenericServerContainerGroupCreateException extends ECFException {

	private static final long serialVersionUID = -7527971074366079811L;

	public GenericServerContainerGroupCreateException() {
		super();
	}

	public GenericServerContainerGroupCreateException(IStatus status) {
		super(status);
	}

	public GenericServerContainerGroupCreateException(String message, Throwable cause) {
		super(message, cause);
	}

	public GenericServerContainerGroupCreateException(String message) {
		super(message);
	}

	public GenericServerContainerGroupCreateException(Throwable cause) {
		super(cause);
	}

}
