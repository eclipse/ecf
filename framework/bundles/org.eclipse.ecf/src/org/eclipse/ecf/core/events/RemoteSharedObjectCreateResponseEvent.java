package org.eclipse.ecf.core.events;

import org.eclipse.ecf.core.identity.ID;

public class RemoteSharedObjectCreateResponseEvent extends RemoteSharedObjectEvent
		implements ISharedObjectCreateResponseEvent {

	long sequence = 0;
	
    public RemoteSharedObjectCreateResponseEvent(ID senderObj, ID remoteCont, long seq, Throwable exception) {
    	super(senderObj,remoteCont,exception);
    	this.sequence = seq;
    }
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.events.ISharedObjectCreateResponseEvent#getSequence()
	 */
	public long getSequence() {
		return sequence;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.events.ISharedObjectCreateResponseEvent#getException()
	 */
	public Throwable getException() {
		return (Throwable) getData();
	}

}
