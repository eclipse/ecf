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
package org.eclipse.ecf.examples.remoteservices.hello;

import java.io.Serializable;

/**
 * @since 3.0
 */
public class HelloMessage implements Serializable {

	private static final long serialVersionUID = 2337422863249201241L;

	private String from;
	private String message;
	
	public HelloMessage(String from, String message) {
		this.from = from;
		this.message = message;
	}
	
	public String getFrom() {
		return from;
	}
	
	public String getMessage() {
		return message;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("HelloMessage[from=");
		builder.append(from);
		builder.append(", message=");
		builder.append(message);
		builder.append("]");
		return builder.toString();
	}
	
}
