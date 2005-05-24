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

public class Commit implements Serializable {

	private static final long serialVersionUID = 3258126938529740848L;

	final Version version;

	public Commit(Version version) {
		this.version = version;
	}

	public Version getVersion() {
		return version;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("Commit[version=");
		buf.append(version).append(']');
		return buf.toString();
	}
}