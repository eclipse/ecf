/*******************************************************************************
 * Copyright (c) 2005 Peter Nehrer and Composent, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Peter Nehrer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.datashare.multicast;

import java.io.Serializable;

public class Message implements Serializable {

	private static final long serialVersionUID = 3257281414121993014L;

	final Version version;

	final Object data;

	public Message(Version version, Object data) {
		this.version = version;
		this.data = data;
	}

	public Version getVersion() {
		return version;
	}

	public Object getData() {
		return data;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("Message[version=");
		buf.append(version).append(";data=");
		buf.append(data).append(']');
		return buf.toString();
	}
}