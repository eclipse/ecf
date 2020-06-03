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
package org.eclipse.ecf.core;

/**
 * Exception class to be thrown upon authentication failure during connect
 * 
 * @see IContainer#connect(org.eclipse.ecf.core.identity.ID,
 *      org.eclipse.ecf.core.security.IConnectContext)
 */
public class ContainerAuthenticationException extends ContainerConnectException {
	private static final long serialVersionUID = 7038962779623213444L;

	public ContainerAuthenticationException() {
		super();
	}

	public ContainerAuthenticationException(String message) {
		super(message);
	}

	public ContainerAuthenticationException(Throwable cause) {
		super(cause);
	}

	public ContainerAuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}

}
