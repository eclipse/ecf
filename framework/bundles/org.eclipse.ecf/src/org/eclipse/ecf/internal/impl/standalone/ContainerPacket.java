package org.eclipse.ecf.internal.impl.standalone;

import org.eclipse.ecf.core.identity.ID;

import java.io.Serializable;

public final class ContainerPacket implements Serializable {
	static final long serialVersionUID = 8416382883801007164L;
	ID fromID;
	public ID toID;
	long sequence;
	byte msg;
	Serializable theData;

	ContainerPacket(ID from, ID to, long seq, byte m, Serializable data) {
		fromID = from;
		toID = to;
		sequence = seq;
		msg = m;
		theData = data;
	}
}
