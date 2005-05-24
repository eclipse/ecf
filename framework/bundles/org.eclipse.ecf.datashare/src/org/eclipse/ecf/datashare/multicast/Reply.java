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

public class Reply implements Serializable {

	private static final long serialVersionUID = 3689632497314837046L;

	final Version version;

	final boolean granted;

	public Reply(Version version, boolean granted) {
		this.version = version;
		this.granted = granted;
	}

	public Version getVersion() {
		return version;
	}

	public boolean isGranted() {
		return granted;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("Reply[version=");
		buf.append(version).append(";granted=");
		buf.append(granted).append(']');
		return buf.toString();
	}
}