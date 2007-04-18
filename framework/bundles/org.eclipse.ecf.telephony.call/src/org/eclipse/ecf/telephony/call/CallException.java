/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.telephony.call;

import org.eclipse.ecf.core.util.ECFException;

/**
 * Exception thrown by Call API calls.
 */
public class CallException extends ECFException {

	private static final long serialVersionUID = -4189098435363433141L;

	public CallException() {
	}

	/**
	 * @param message
	 */
	public CallException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public CallException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public CallException(String message, Throwable cause) {
		super(message, cause);
	}

}
