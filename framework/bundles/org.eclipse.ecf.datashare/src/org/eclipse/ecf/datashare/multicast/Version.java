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

import org.eclipse.ecf.core.identity.ID;

public class Version implements Serializable {

	private static final long serialVersionUID = 3762538901495101236L;

	private final ID senderID;

	private final long sequence;

	public Version(ID senderID, long sequence) {
		this.senderID = senderID;
		this.sequence = sequence;
	}

	public ID getSenderID() {
		return senderID;
	}

	public long getSequence() {
		return sequence;
	}

	public boolean equals(Object other) {
		if (other instanceof Version) {
			Version o = (Version) other;
			return senderID.equals(o.senderID) && sequence == o.sequence;
		} else
			return false;
	}

	public int hashCode() {
		int c = 17;
		c = 37 * c + senderID.hashCode();
		c = 37 * c + (int) sequence;
		return c;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("Version[senderID=");
		buf.append(senderID).append(";sequence=");
		buf.append(sequence).append(']');
		return buf.toString();
	}
}