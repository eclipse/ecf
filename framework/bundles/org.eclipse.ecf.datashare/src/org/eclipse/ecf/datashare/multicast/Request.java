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

public class Request implements Serializable {

	private static final long serialVersionUID = 3257003237730365493L;

	final Version version;

	public Request(Version version) {
		this.version = version;
	}

	public Version getVersion() {
		return version;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("Request[version=");
		buf.append(version).append(']');
		return buf.toString();
	}
}