/*******************************************************************************
 * Copyright (c) 2010 Naumen. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Pavel Samolisov - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.remoteservice.rpc;

import org.eclipse.ecf.core.util.ECFException;

public class RPCException extends ECFException {

	private static final long serialVersionUID = 5822049943586453183L;

	public RPCException(Throwable cause) {
		super(cause);
	}

	public RPCException(Throwable cause, int errorCode) {
		super(cause);
	}

	public RPCException(String message, Throwable cause) {
		super(message, cause);
	}
}
