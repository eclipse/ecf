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

import org.eclipse.ecf.core.identity.ID;

/**
 * @author pnehrer
 */
public class Version implements Serializable {

	private static final long serialVersionUID = 3258415036413456951L;

	private final long sequence;

    private final ID containerID;

    public Version(ID sourceID) {
        this(0, sourceID);
    }

    private Version(long sequence, ID sourceID) {
        this.sequence = sequence;
        this.containerID = sourceID;
    }

    public long getSequence() {
        return sequence;
    }

    public ID getContainerID() {
        return containerID;
    }

    public Version getNext(ID sourceID) {
        return new Version(sequence + 1, sourceID);
    }

    public boolean equals(Object other) {
        if (other instanceof Version) {
            Version o = (Version) other;
            return sequence == o.sequence && containerID.equals(o.containerID);
        } else
            return false;
    }

    public int hashCode() {
        int c = 17;
        c = 37 * c + (int) sequence;
        c = 37 * c + containerID.hashCode();
        return c;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("Version[");
        buf.append("sequence=").append(sequence).append(";");
        buf.append("containerID=").append(containerID).append("]");
        return buf.toString();
    }
}
