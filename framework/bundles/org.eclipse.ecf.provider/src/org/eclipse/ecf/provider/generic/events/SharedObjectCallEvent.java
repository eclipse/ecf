package org.eclipse.ecf.provider.generic.events;

import org.eclipse.ecf.core.events.ISharedObjectCallEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.AsynchResult;
import org.eclipse.ecf.core.util.Event;

public class SharedObjectCallEvent implements ISharedObjectCallEvent {

    ID sender;
    Event event;
    AsynchResult result;
    
    public SharedObjectCallEvent(ID sender, Event evt, AsynchResult res) {
        super();
        this.sender = sender;
        this.event = evt;
        this.result = res;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.events.ISharedObjectCallEvent#getAsynchResult()
     */
    public AsynchResult getAsynchResult() {
        return result;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.events.ISharedObjectEvent#getSenderSharedObjectID()
     */
    public ID getSenderSharedObjectID() {
        return sender;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.events.ISharedObjectEvent#getEvent()
     */
    public Event getEvent() {
        return event;
    }

}
