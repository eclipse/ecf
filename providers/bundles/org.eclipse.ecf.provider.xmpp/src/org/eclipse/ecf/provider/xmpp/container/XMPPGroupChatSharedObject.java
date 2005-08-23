/*
 * Created on Mar 21, 2005
 *
 */
package org.eclipse.ecf.provider.xmpp.container;

import java.util.Iterator;
import java.util.Vector;
import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.ISharedObjectConfig;
import org.eclipse.ecf.core.ISharedObjectContext;
import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.presence.IMessageListener;
import org.eclipse.ecf.provider.xmpp.Trace;
import org.eclipse.ecf.provider.xmpp.events.MessageEvent;
import org.jivesoftware.smack.packet.Message;

public class XMPPGroupChatSharedObject implements ISharedObject {

    public static Trace trace = Trace.create("xmppgroupchatsharedobject");

    ISharedObjectConfig config = null;
    
    Vector messageListeners = new Vector();
	Namespace usernamespace = null;

    protected void debug(String msg) {
        if (Trace.ON && trace != null) {
            trace.msg(config.getSharedObjectID() + ":" + msg);
        }
    }
    protected void dumpStack(String msg, Throwable e) {
        if (Trace.ON && trace != null) {
            trace.dumpStack(e, config.getSharedObjectID() + ":" + msg);
        }
    }

    protected void addMessageListener(IMessageListener listener) {
        messageListeners.add(listener);
    }
    protected void removeMessageListener(IMessageListener listener) {
        messageListeners.add(listener);
    }

    public XMPPGroupChatSharedObject(Namespace usernamespace) {
        super();
        this.usernamespace = usernamespace;
    }
    protected ISharedObjectContext getContext() {
        return config.getContext();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObject#init(org.eclipse.ecf.core.ISharedObjectConfig)
     */
    public void init(ISharedObjectConfig initData)
            throws SharedObjectInitException {
        this.config = initData;
    }

    protected ID makeUserIDFromName(String name) {
        ID result = null;
        try {
            result = IDFactory.getDefault().makeID(usernamespace, new Object[] { name });
            return result;
        } catch (Exception e) {
            dumpStack("Exception in makeIDFromName", e);
            return null;
        }
    }

    protected Message.Type [] ALLOWED_MESSAGES = { Message.Type.GROUP_CHAT };
    protected Message filterMessageType(Message msg) {
    	for(int i=0; i < ALLOWED_MESSAGES.length; i++) {
    		if (ALLOWED_MESSAGES[i].equals(msg.getType())) {
    			return msg;
    		}
    	}
    	return null;
    }
    protected String canonicalizeRoomFrom(String from) {
        if (from == null)
            return null;
        int atIndex = from.indexOf('@');
        String hostname = null;
        String username = null;
        int index = from.indexOf("/");
        if (atIndex > 0 && index > 0) {
        	hostname = from.substring(atIndex+1,index);
        	int dotIndex = hostname.lastIndexOf('.');
        	if (dotIndex > 0) {
        		hostname = hostname.substring(dotIndex+1);
        	}
            username = from.substring(index+1);
            return username+"@"+hostname;
        }
        return from;
    }
    protected IMessageListener.Type makeMessageType(Message.Type type) {
        if (type == null)
            return IMessageListener.Type.NORMAL;
        if (type == Message.Type.CHAT) {
            return IMessageListener.Type.CHAT;
        } else if (type == Message.Type.NORMAL) {
            return IMessageListener.Type.NORMAL;
        } else if (type == Message.Type.GROUP_CHAT) {
            return IMessageListener.Type.GROUP_CHAT;
        } else if (type == Message.Type.HEADLINE) {
            return IMessageListener.Type.HEADLINE;
        } else if (type == Message.Type.HEADLINE) {
            return IMessageListener.Type.HEADLINE;
        } else
            return IMessageListener.Type.NORMAL;
    }
    protected void fireMessage(ID from, ID to, IMessageListener.Type type,
            String subject, String body) {
        for (Iterator i = messageListeners.iterator(); i.hasNext();) {
            IMessageListener l = (IMessageListener) i.next();
            l.handleMessage(from, to, type, subject, body);
        }
    }
    protected String canonicalizeRoomTo(String to) {
        if (to == null)
            return null;
        int index = to.indexOf("/");
        if (index > 0) {
            return to.substring(0, index);
        } else
            return to;
    }



    protected void handleMessageEvent(MessageEvent evt) {
        Message msg = evt.getMessage();
        String from = msg.getFrom();
        String to = msg.getTo();
        String body = msg.getBody();
        String subject = msg.getSubject();
        ID fromID = makeUserIDFromName(canonicalizeRoomFrom(from));
        ID toID = makeUserIDFromName(canonicalizeRoomTo(to));
        msg = filterMessageType(msg);
        if (msg != null) fireMessage(fromID, toID, makeMessageType(msg.getType()), subject, body);
    }


    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObject#handleEvent(org.eclipse.ecf.core.util.Event)
     */
    public void handleEvent(Event event) {
        debug("handleEvent(" + event + ")");
        if (event instanceof MessageEvent) {
            handleMessageEvent((MessageEvent) event);
        } else {
            debug("unrecognized event " + event);
        }
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
        messageListeners.clear();
        this.config = null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObject#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class clazz) {
        return null;
    }
}
