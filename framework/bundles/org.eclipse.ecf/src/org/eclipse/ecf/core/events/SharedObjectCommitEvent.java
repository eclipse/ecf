package org.eclipse.ecf.core.events;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.Event;

public class SharedObjectCommitEvent implements ISharedObjectCommitEvent {

	private static final long serialVersionUID = 4615634472917480497L;

	ID senderSharedObjectID = null;
	Event event = null;
	
	public SharedObjectCommitEvent(ID senderSharedObjectID, Event event) {
		super();
		this.senderSharedObjectID = senderSharedObjectID;
		this.event = event;
	}
	
	public SharedObjectCommitEvent(ID senderSharedObjectID) {
		this(senderSharedObjectID,null);
	}

	public ID getSenderSharedObjectID() {
		return senderSharedObjectID;
	}

	public Event getEvent() {
		return event;
	}

    public String toString() {
        StringBuffer sb = new StringBuffer("SharedObjectCommitEvent[");
        sb.append(getSenderSharedObjectID()).append(";");
        sb.append(getEvent()).append("]");
        return sb.toString();
    }

}
