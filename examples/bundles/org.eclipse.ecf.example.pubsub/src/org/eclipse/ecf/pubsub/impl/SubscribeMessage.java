package org.eclipse.ecf.pubsub.impl;

import java.io.Serializable;

import org.eclipse.ecf.core.identity.ID;

public class SubscribeMessage implements Serializable {

	private static final long serialVersionUID = -8507642983243509135L;
	
	private final ID requestorID;
	
	public SubscribeMessage(ID requestorID) {
		this.requestorID = requestorID;
	}
	
	public ID getRequestorID() {
		return requestorID;
	}

	public int hashCode() {
		return requestorID.hashCode();
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;

		SubscribeMessage other = (SubscribeMessage) obj;
		return requestorID.equals(other.requestorID);
	}
}
