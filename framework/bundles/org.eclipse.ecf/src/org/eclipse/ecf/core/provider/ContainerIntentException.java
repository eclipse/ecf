/*******************************************************************************
* Copyright (c) 2018 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.core.provider;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.ContainerCreateException;

/**
 * @since 3.9
 */
public class ContainerIntentException extends ContainerCreateException {

	private String intentName;

	public ContainerIntentException(String intentName, IStatus status) {
		super(status);
		this.intentName = intentName;
	}

	public ContainerIntentException(String intentName, String message, Throwable cause) {
		super(message, cause);
		this.intentName = intentName;
	}

	public ContainerIntentException(String intentName, String message) {
		super(message);
		this.intentName = intentName;
	}

	private static final long serialVersionUID = -2199528348944072112L;

	public String getIntentName() {
		return this.intentName;
	}

}
