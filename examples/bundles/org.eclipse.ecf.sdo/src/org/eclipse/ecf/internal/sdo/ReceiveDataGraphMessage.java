/*******************************************************************************
 * Copyright (c) 2004 Peter Nehrer and Composent, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Peter Nehrer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.internal.sdo;

import java.io.Serializable;

/**
 * @author pnehrer
 */
public class ReceiveDataGraphMessage implements Serializable {

	private static final long serialVersionUID = 3617293441302606899L;

	private final Version version;

	private final Object data;

	public ReceiveDataGraphMessage(Version version, Object data) {
		this.version = version;
		this.data = data;
	}

	public Version getVersion() {
		return version;
	}

	public Object getData() {
		return data;
	}

	public boolean equals(Object other) {
		if (other instanceof ReceiveDataGraphMessage) {
			ReceiveDataGraphMessage o = (ReceiveDataGraphMessage) other;
			return version.equals(o.version) && data.equals(o.data);
		} else
			return false;
	}

	public int hashCode() {
		int c = 17;
		c = 37 * c + version.hashCode();
		c = 37 * c + data.hashCode();
		return c;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("ReceiveDataGraphMessage[");
		buf.append("version=").append(version).append(";");
		buf.append("data=").append(data).append("]");
		return buf.toString();
	}
}
