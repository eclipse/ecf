/*
 * Created on Mar 21, 2005
 *
 */
package org.eclipse.ecf.provider.xmpp.container;

import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.ISharedObjectConfig;
import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.provider.xmpp.Trace;

public class XMPPGroupChatSharedObject implements ISharedObject {

    public static Trace trace = Trace.create("xmppgroupchatsharedobject");

    ISharedObjectConfig config = null;
    
    protected void debug(String msg) {
        if (Trace.ON && trace != null) {
            trace.msg(config.getSharedObjectID() + ":" + msg);
        }
    }

    public XMPPGroupChatSharedObject() {
        super();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObject#init(org.eclipse.ecf.core.ISharedObjectConfig)
     */
    public void init(ISharedObjectConfig initData)
            throws SharedObjectInitException {
        this.config = initData;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObject#handleEvent(org.eclipse.ecf.core.util.Event)
     */
    public void handleEvent(Event event) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObject#handleEvents(org.eclipse.ecf.core.util.Event[])
     */
    public void handleEvents(Event[] events) {
        for(int i=0; i < events.length; i++) {
            this.handleEvent(events[i]);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObject#dispose(org.eclipse.ecf.core.identity.ID)
     */
    public void dispose(ID containerID) {
        this.config = null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObject#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class clazz) {
        return null;
    }
}
