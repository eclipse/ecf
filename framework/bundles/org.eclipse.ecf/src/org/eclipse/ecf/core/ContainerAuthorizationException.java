/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core;

public class ContainerAuthorizationException extends ContainerConnectException {
	private static final long serialVersionUID = 7038962779623213444L;
	public ContainerAuthorizationException() {
		super();
	}
	/**
	 * @param message
	 */
	public ContainerAuthorizationException(String message) {
		super(message);
	}
	/**
	 * @param cause
	 */
	public ContainerAuthorizationException(Throwable cause) {
		super(cause);
	}
	/**
	 * @param message
	 * @param cause
	 */
	public ContainerAuthorizationException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
