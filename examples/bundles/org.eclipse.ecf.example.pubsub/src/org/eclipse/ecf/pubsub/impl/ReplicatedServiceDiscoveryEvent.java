/**
 * Copyright (c) 2006 Ecliptical Software Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ecliptical Software Inc. - initial API and implementation
 */
package org.eclipse.ecf.pubsub.impl;

import java.util.Arrays;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.Event;

public class ReplicatedServiceDiscoveryEvent implements Event {

	private static final long serialVersionUID = 1848459358595071814L;
	
	public static final int ADDED = 0;
	
	public static final int REMOVED = 1;
	
	private final int kind; 

	private final ID containerID;
	
	private final ID[] sharedObjectIDs;
	
	public ReplicatedServiceDiscoveryEvent(int kind, ID containerID, ID[] sharedObjectIDs) {
		this.kind = kind;
		this.containerID = containerID;
		this.sharedObjectIDs = sharedObjectIDs;
	}

	public int getKind() {
		return kind;
	}

	public ID getContainerID() {
		return containerID;
	}

	public ID[] getSharedObjectIDs() {
		return sharedObjectIDs;
	}

	public int hashCode() {
		int c = 17;
		c = 37 * c + kind;
		c = 37 * c + containerID.hashCode();
		c = 37 * c + sharedObjectIDs[0].hashCode();
		return c;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;

		ReplicatedServiceDiscoveryEvent other = (ReplicatedServiceDiscoveryEvent) obj;
		return kind == other.kind && containerID.equals(other.containerID) && Arrays.equals(sharedObjectIDs, other.sharedObjectIDs);
	}
}
