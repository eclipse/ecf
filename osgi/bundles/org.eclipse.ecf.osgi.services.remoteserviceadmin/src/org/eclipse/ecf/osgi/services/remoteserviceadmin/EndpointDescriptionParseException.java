/*******************************************************************************
 * Copyright (c) 2010-2011 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

public class EndpointDescriptionParseException extends Exception {

	private static final long serialVersionUID = -4481979787400184664L;

	public EndpointDescriptionParseException() {
	}

	public EndpointDescriptionParseException(String message) {
		super(message);
	}

	public EndpointDescriptionParseException(Throwable cause) {
		super(cause);
	}

	public EndpointDescriptionParseException(String message, Throwable cause) {
		super(message, cause);
	}

}
