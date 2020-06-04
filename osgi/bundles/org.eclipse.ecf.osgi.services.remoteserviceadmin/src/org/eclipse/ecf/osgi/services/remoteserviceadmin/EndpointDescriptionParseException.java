/****************************************************************************
 * Copyright (c) 2010-2011 Composent, Inc. and others.
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
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

/**
 * Exception class to represent endpoint description parse problems when
 * performed by {@link IEndpointDescriptionReader}.
 * 
 * @see IEndpointDescriptionReader#readEndpointDescriptions(java.io.InputStream)
 */
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
