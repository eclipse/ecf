/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.xmpp.container;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.ecf.core.events.IContainerConnectedEvent;
import org.eclipse.ecf.core.events.IContainerDisconnectedEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.sharedobject.ISharedObject;
import org.eclipse.ecf.core.sharedobject.ISharedObjectConfig;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContext;
import org.eclipse.ecf.core.sharedobject.SharedObjectInitException;
import org.eclipse.ecf.core.sharedobject.events.ISharedObjectActivatedEvent;
import org.eclipse.ecf.core.sharedobject.events.ISharedObjectDeactivatedEvent;
import org.eclipse.ecf.core.sharedobject.events.ISharedObjectMessageEvent;
import org.eclipse.ecf.core.sharedobject.events.ISharedObjectMessageListener;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.internal.provider.xmpp.Trace;
import org.eclipse.ecf.presence.IAccountManager;
import org.eclipse.ecf.presence.IMessageListener;
import org.eclipse.ecf.presence.IPresence;
import org.eclipse.ecf.presence.IPresenceListener;
import org.eclipse.ecf.presence.IRosterEntry;
import org.eclipse.ecf.presence.IRosterGroup;
import org.eclipse.ecf.presence.IRosterSubscriptionListener;
import org.eclipse.ecf.presence.chat.IInvitationListener;
import org.eclipse.ecf.provider.xmpp.events.IQEvent;
import org.eclipse.ecf.provider.xmpp.events.InvitationReceivedEvent;
import org.eclipse.ecf.provider.xmpp.events.MessageEvent;
import org.eclipse.ecf.provider.xmpp.events.PresenceEvent;
import org.eclipse.ecf.provider.xmpp.identity.XMPPID;
import org.eclipse.ecf.provider.xmpp.identity.XMPPRoomID;
import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;

public class XMPPPresenceSharedObject implements ISharedObject, IAccountManager {
    
    public static Trace trace = Trace.create("xmpppresencesharedobject");
    ISharedObjectConfig config = null;
    XMPPConnection connection = null;
	AccountManager accountManager = null;
    Vector messageListeners = new Vector();
    Vector presenceListeners = new Vector();
    Vector sharedObjectMessageListeners = new Vector();
    Vector subscribeListeners = new Vector();
    Vector invitationListeners = new Vector();
	Namespace namespace = null;
	
    protected void fireInvitationReceived(ID roomID, ID fromID, ID toID, String subject, String body) {
        for (Iterator i = invitationListeners.iterator(); i.hasNext();) {
            IInvitationListener l = (IInvitationListener) i.next();
            l.handleInvitationReceived(roomID,fromID,toID,subject,body);
        }
    }

    protected void addInvitationListener(IInvitationListener listener) {
        invitationListeners.add(listener);
    }
    protected void removeInvitationListener(IInvitationListener listener) {
    	invitationListeners.remove(listener);
    }
    protected void addPresenceListener(IPresenceListener listener) {
        presenceListeners.add(listener);
    }
    protected void removePresenceListener(IPresenceListener listener) {
        presenceListeners.remove(listener);
    }
    protected void addMessageListener(IMessageListener listener) {
        messageListeners.add(listener);
    }
    protected void removeMessageListener(IMessageListener listener) {
        messageListeners.add(listener);
    }
    protected void addSharedObjectMessageListener(ISharedObjectMessageListener listener) {
        sharedObjectMessageListeners.add(listener);
    }
    protected void removeSharedObjectMessageListener(ISharedObjectMessageListener listener) {
        sharedObjectMessageListeners.remove(listener);
    }
	protected void addSubscribeListener(IRosterSubscriptionListener listener) {
		subscribeListeners.add(listener);
	}
	protected void removeSubscribeListener(IRosterSubscriptionListener listener) {
		subscribeListeners.remove(listener);
	}
    protected String canonicalizePresenceFrom(String from) {
        if (from == null)
            return null;
        else return from;
        /*
        int index = from.indexOf("/");
        if (index > 0) {
            return from.substring(0, index);
        } else
            return from;
            */
    }

    protected void debug(String msg) {
        if (Trace.ON && trace != null) {
            trace.msg(config.getSharedObjectID() + ":" + msg);
        }
    }

    protected void disconnect() {
        ISharedObjectContext context = getContext();
        if (context != null) {
            context.disconnect();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObject#dispose(org.eclipse.ecf.core.identity.ID)
     */
    public void dispose(ID containerID) {
        config = null;
		accountManager = null;
        invitationListeners.clear();
        invitationListeners = null;
    }

    protected void dumpStack(String msg, Throwable e) {
        if (Trace.ON && trace != null) {
            trace.dumpStack(e, config.getSharedObjectID() + ":" + msg);
        }
    }

    protected void fireContainerDeparted(ID departed) {
        for (Iterator i = presenceListeners.iterator(); i.hasNext();) {
            IPresenceListener l = (IPresenceListener) i.next();
            l.handleDisconnected(departed);
        }
    }

    protected void fireContainerJoined(ID containerJoined) {
        for (Iterator i = presenceListeners.iterator(); i.hasNext();) {
            IPresenceListener l = (IPresenceListener) i.next();
            l.handleConnected(containerJoined);
        }
    }

    protected void fireMessage(ID from, ID to, IMessageListener.Type type,
            String subject, String body) {
        for (Iterator i = messageListeners.iterator(); i.hasNext();) {
            IMessageListener l = (IMessageListener) i.next();
            l.handleMessage(from, to, type, subject, body);
        }
    }

    protected void firePresence(ID fromID, IPresence presence) {
        for (Iterator i = presenceListeners.iterator(); i.hasNext();) {
            IPresenceListener l = (IPresenceListener) i.next();
            l.handlePresence(fromID, presence);
        }
    }

    protected void fireSubscribe(ID fromID, IPresence presence) {
        for (Iterator i = subscribeListeners.iterator(); i.hasNext();) {
            IRosterSubscriptionListener l = (IRosterSubscriptionListener) i.next();
			if (presence.getType().equals(IPresence.Type.SUBSCRIBE)) {
				l.handleSubscribeRequest(fromID);
			} else if (presence.getType().equals(IPresence.Type.SUBSCRIBED)) {
				l.handleSubscribed(fromID);
			} else if (presence.getType().equals(IPresence.Type.UNSUBSCRIBED)) {
				l.handleUnsubscribed(fromID);
			}
        }
    }

    protected void fireSetRosterEntry(IRosterEntry entry) {
        for (Iterator i = presenceListeners.iterator(); i.hasNext();) {
            IPresenceListener l = (IPresenceListener) i.next();
			if (entry.getInterestType() == IRosterEntry.InterestType.REMOVE
					|| entry.getInterestType() == IRosterEntry.InterestType.NONE) l.handleRosterEntryRemove(entry);
            else l.handleRosterEntryUpdate(entry);
        }
    }
    protected void fireRosterEntry(IRosterEntry entry) {
        for (Iterator i = presenceListeners.iterator(); i.hasNext();) {
            IPresenceListener l = (IPresenceListener) i.next();
            l.handleRosterEntryAdd(entry);
        }
    }

    protected void fireSharedObjectMessage(ISharedObjectMessageEvent event) {
        for (Iterator i = sharedObjectMessageListeners.iterator(); i.hasNext();) {
            ISharedObjectMessageListener l = (ISharedObjectMessageListener) i.next();
            l.handleSharedObjectMessage(event);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObject#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class clazz) {
        return null;
    }

    protected XMPPConnection getConnection() {
        return connection;
    }

    protected ISharedObjectContext getContext() {
        return config.getContext();
    }

    protected String getUserNameFromXMPPAddress(XMPPID userID) {
        return userID.getUsername();
    }

    protected void handleContainerDepartedEvent(
            IContainerDisconnectedEvent event) {
        ID departedID = event.getTargetID();
        if (departedID != null) {
            fireContainerDeparted(departedID);
        }
    }

    protected void handleDeactivatedEvent(ISharedObjectDeactivatedEvent event) {
        debug("Got deactivated event: " + event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObject#handleEvent(org.eclipse.ecf.core.util.Event)
     */
    public void handleEvent(Event event) {
        debug("handleEvent(" + event + ")");
        if (event instanceof ISharedObjectActivatedEvent) {
        } else if (event instanceof IContainerConnectedEvent) {
            handleJoin((IContainerConnectedEvent) event);
        } else if (event instanceof IQEvent) {
            handleIQEvent((IQEvent) event);
        } else if (event instanceof MessageEvent) {
            handleMessageEvent((MessageEvent) event);
        } else if (event instanceof PresenceEvent) {
            handlePresenceEvent((PresenceEvent) event);
        } else if (event instanceof InvitationReceivedEvent) {
        	handleInvitationEvent((InvitationReceivedEvent) event);
        } else if (event instanceof ISharedObjectDeactivatedEvent) {
            handleDeactivatedEvent((ISharedObjectDeactivatedEvent) event);
        } else if (event instanceof IContainerDisconnectedEvent) {
            handleContainerDepartedEvent((IContainerDisconnectedEvent) event);
        } else if (event instanceof ISharedObjectMessageEvent) {
            fireSharedObjectMessage((ISharedObjectMessageEvent) event);
        } else {
            debug("unrecognized event " + event);
        }
    }

    protected ID createRoomIDFromName(String from) {
    	try {
    		return new XMPPRoomID(namespace,connection,from);
    	} catch (URISyntaxException e) {
            dumpStack("Exception in createRoomIDFromName", e);
            return null;
    	}
    }
    protected ID createUserIDFromName(String name) {
        ID result = null;
        try {
            result = new XMPPID(namespace,name);
            return result;
        } catch (Exception e) {
            dumpStack("Exception in createIDFromName", e);
            return null;
        }
    }

    protected void handleInvitationEvent(InvitationReceivedEvent event) {
    	XMPPConnection conn = event.getConnection();
    	if (conn == connection) {
	    	ID roomID = createRoomIDFromName(event.getRoom());
	    	ID fromID = createUserIDFromName(event.getInviter());
	    	Message mess = event.getMessage();
	    	ID toID = createUserIDFromName(mess.getTo());
	    	String subject = mess.getSubject();
	    	String body = event.getReason();
	    	fireInvitationReceived(roomID,fromID,toID,subject,body);
    	} else {
    		debug("got invitation event for other connection "+event);
    	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObject#handleEvents(org.eclipse.ecf.core.util.Event[])
     */
    public void handleEvents(Event[] events) {
        for (int i = 0; i < events.length; i++) {
            handleEvent(events[i]);
        }
    }

    protected void handleIQEvent(IQEvent evt) {
		IQ iq = evt.getIQ();
		if (iq instanceof RosterPacket) {
			// Roster packet...report to UI
			RosterPacket rosterPacket = (RosterPacket) iq;
			if (rosterPacket.getType() == IQ.Type.SET
					|| rosterPacket.getType() == IQ.Type.RESULT) {
				for (Iterator i = rosterPacket.getRosterItems(); i.hasNext();) {
					IRosterEntry entry = createRosterEntry((RosterPacket.Item) i
							.next());
					fireSetRosterEntry(entry);
				}
			}
		} else {
			debug("Received unknown IQ message: " + iq.toXML());
		}
	}

    protected void handleJoin(IContainerConnectedEvent event) {
        fireContainerJoined(event.getTargetID());
    }
    
    protected Message.Type [] ALLOWED_MESSAGES = { Message.Type.CHAT, Message.Type.ERROR, Message.Type.HEADLINE, Message.Type.NORMAL };
    protected Message filterMessageType(Message msg) {
    	for(int i=0; i < ALLOWED_MESSAGES.length; i++) {
    		if (ALLOWED_MESSAGES[i].equals(msg.getType())) {
    			return msg;
    		}
    	}
    	return null;
    }
    protected void handleMessageEvent(MessageEvent evt) {
        Message msg = evt.getMessage();
        String from = msg.getFrom();
        String to = msg.getTo();
        String body = msg.getBody();
        String subject = msg.getSubject();
        ID fromID = createIDFromName(canonicalizePresenceFrom(from));
        ID toID = createIDFromName(canonicalizePresenceFrom(to));
        msg = filterMessageType(msg);
        if (msg != null) fireMessage(fromID, toID, createMessageType(msg.getType()), subject, body);
    }

    protected void handlePresenceEvent(PresenceEvent evt) {
        Presence xmppPresence = evt.getPresence();
        String from = canonicalizePresenceFrom(xmppPresence.getFrom());
        IPresence newPresence = createIPresence(xmppPresence);
        ID fromID = createIDFromName(from);
		if (newPresence.getType().equals(IPresence.Type.SUBSCRIBE) || 
				newPresence.getType().equals(IPresence.Type.UNSUBSCRIBE) ||
				newPresence.getType().equals(IPresence.Type.SUBSCRIBED) ||
				newPresence.getType().equals(IPresence.Type.UNSUBSCRIBED)) {
			fireSubscribe(fromID,newPresence);
		} else firePresence(fromID, newPresence);
    }

    protected void handleRoster(Roster roster) {
        for (Iterator i = roster.getEntries(); i.hasNext();) {
            IRosterEntry entry = createRosterEntry((RosterEntry) i.next());
            fireRosterEntry(entry);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObject#init(org.eclipse.ecf.core.ISharedObjectConfig)
     */
    public void init(ISharedObjectConfig initData)
            throws SharedObjectInitException {
        this.config = initData;
        this.namespace = getContext().getConnectNamespace();
    }

    protected ID createIDFromName(String name) {
        ID result = null;
        try {
            result = IDFactory.getDefault().createID(namespace, new Object[] { name });
            return result;
        } catch (Exception e) {
            dumpStack("Exception in createIDFromName", e);
            return null;
        }
    }

    protected IRosterEntry.InterestType createInterestType(
            RosterPacket.ItemType itemType) {
        if (itemType == RosterPacket.ItemType.BOTH) {
            return IRosterEntry.InterestType.BOTH;
        } else if (itemType == RosterPacket.ItemType.FROM) {
            return IRosterEntry.InterestType.BOTH;
        } else if (itemType == RosterPacket.ItemType.NONE) {
            return IRosterEntry.InterestType.NONE;
        } else if (itemType == RosterPacket.ItemType.REMOVE) {
            return IRosterEntry.InterestType.REMOVE;
        } else if (itemType == RosterPacket.ItemType.TO) {
            return IRosterEntry.InterestType.TO;
        } else
            return IRosterEntry.InterestType.BOTH;
    }

    protected IMessageListener.Type createMessageType(Message.Type type) {
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

    protected IPresence createIPresence(Presence xmppPresence) {
        String status = xmppPresence.getStatus();
        IPresence newPresence = new org.eclipse.ecf.presence.Presence(
                createIPresenceType(xmppPresence), status,
                createIPresenceMode(xmppPresence));
        return newPresence;
    }

    protected Presence createPresence(IPresence ipresence) {
        String status = ipresence.getStatus();
        Presence newPresence = new Presence(createPresenceType(ipresence), status, 0, createPresenceMode(ipresence));
        return newPresence;
    }

    protected IPresence.Mode createIPresenceMode(Presence xmppPresence) {
        if (xmppPresence == null)
            return IPresence.Mode.AVAILABLE;
        Mode mode = xmppPresence.getMode();
        if (mode == Presence.Mode.AVAILABLE) {
            return IPresence.Mode.AVAILABLE;
        } else if (mode == Presence.Mode.AWAY) {
            return IPresence.Mode.AWAY;
        } else if (mode == Presence.Mode.CHAT) {
            return IPresence.Mode.CHAT;
        } else if (mode == Presence.Mode.DO_NOT_DISTURB) {
            return IPresence.Mode.DND;
        } else if (mode == Presence.Mode.EXTENDED_AWAY) {
            return IPresence.Mode.EXTENDED_AWAY;
        } else if (mode == Presence.Mode.INVISIBLE) {
            return IPresence.Mode.INVISIBLE;
        }
        return IPresence.Mode.AVAILABLE;
    }

    protected Presence.Mode createPresenceMode(IPresence ipresence) {
        if (ipresence == null)
            return Presence.Mode.AVAILABLE;
        IPresence.Mode mode = ipresence.getMode();
        if (mode == IPresence.Mode.AVAILABLE) {
            return Presence.Mode.AVAILABLE;
        } else if (mode == IPresence.Mode.AWAY) {
            return Presence.Mode.AWAY;
        } else if (mode == IPresence.Mode.CHAT) {
            return Presence.Mode.CHAT;
        } else if (mode == IPresence.Mode.DND) {
            return Presence.Mode.DO_NOT_DISTURB;
        } else if (mode == IPresence.Mode.EXTENDED_AWAY) {
            return Presence.Mode.EXTENDED_AWAY;
        } else if (mode == IPresence.Mode.INVISIBLE) {
            return Presence.Mode.INVISIBLE;
        }
        return Presence.Mode.AVAILABLE;
    }

    protected IPresence.Type createIPresenceType(Presence xmppPresence) {
        if (xmppPresence == null)
            return IPresence.Type.AVAILABLE;
        Type type = xmppPresence.getType();
        if (type == Presence.Type.AVAILABLE) {
            return IPresence.Type.AVAILABLE;
        } else if (type == Presence.Type.ERROR) {
            return IPresence.Type.ERROR;
        } else if (type == Presence.Type.SUBSCRIBE) {
            return IPresence.Type.SUBSCRIBE;
        } else if (type == Presence.Type.SUBSCRIBED) {
            return IPresence.Type.SUBSCRIBED;
        } else if (type == Presence.Type.UNSUBSCRIBE) {
            return IPresence.Type.UNSUBSCRIBE;
        } else if (type == Presence.Type.UNSUBSCRIBED) {
            return IPresence.Type.UNSUBSCRIBED;
        } else if (type == Presence.Type.UNAVAILABLE) {
            return IPresence.Type.UNAVAILABLE;
        }
        return IPresence.Type.AVAILABLE;
    }

    protected Presence.Type createPresenceType(IPresence ipresence) {
        if (ipresence == null)
            return Presence.Type.AVAILABLE;
        IPresence.Type type = ipresence.getType();
        if (type == IPresence.Type.AVAILABLE) {
            return Presence.Type.AVAILABLE;
        } else if (type == IPresence.Type.ERROR) {
            return Presence.Type.ERROR;
        } else if (type == IPresence.Type.SUBSCRIBE) {
            return Presence.Type.SUBSCRIBE;
        } else if (type == IPresence.Type.SUBSCRIBED) {
            return Presence.Type.SUBSCRIBED;
        } else if (type == IPresence.Type.UNSUBSCRIBE) {
            return Presence.Type.UNSUBSCRIBE;
        } else if (type == IPresence.Type.UNSUBSCRIBED) {
            return Presence.Type.UNSUBSCRIBED;
        } else if (type == IPresence.Type.UNAVAILABLE) {
            return Presence.Type.UNAVAILABLE;
        }
        return Presence.Type.AVAILABLE;
    }

    protected IRosterEntry createRosterEntry(RosterEntry entry) {
        try {
            ID userID = createIDFromName(entry.getUser());
            String name = entry.getName();
            RosterPacket.ItemType itemType = entry.getType();
            IRosterEntry.InterestType iType = createInterestType(itemType);
            ID svcID = getContext().getConnectedID();
            IRosterEntry newEntry = new org.eclipse.ecf.presence.RosterEntry(svcID,
                    userID, name, iType);
            Iterator grps = entry.getGroups();
            for (; grps.hasNext();) {
                RosterGroup grp = (RosterGroup) grps.next();
                IRosterGroup localGrp = createRosterGroup(grp);
                newEntry.add(localGrp);
            }
            return newEntry;
        } catch (Exception e) {
            dumpStack("Exception in createRosterEntry", e);
        }
        return null;
    }

    protected IRosterEntry createRosterEntry(RosterPacket.Item entry) {
        try {
            ID userID = createIDFromName(entry.getUser());
            String name = entry.getName();
            RosterPacket.ItemType itemType = entry.getItemType();
            IRosterEntry.InterestType iType = createInterestType(itemType);
            ID svcID = getContext().getConnectedID();
            IRosterEntry newEntry = new org.eclipse.ecf.presence.RosterEntry(svcID,
                    userID, name, iType);
            Iterator grps = entry.getGroupNames();
            for (; grps.hasNext();) {
                String grp = (String) grps.next();
                IRosterGroup localGrp = createRosterGroup(grp);
                newEntry.add(localGrp);
            }
            return newEntry;
        } catch (Exception e) {
            dumpStack("Exception in createRosterEntry", e);
        }
        return null;
    }

    protected IRosterGroup createRosterGroup(RosterGroup grp) {
        return new org.eclipse.ecf.presence.RosterGroup(grp.getName());
    }

    protected IRosterGroup createRosterGroup(String grp) {
        return new org.eclipse.ecf.presence.RosterGroup(grp);
    }

    protected void setConnection(XMPPConnection connection) {
        this.connection = connection;
		if (connection != null) {
			accountManager = new AccountManager(connection);
		}
    }
	public boolean changePassword(String newpassword) throws ECFException {
		if (accountManager == null) throw new ECFException("not connected");
		try {
			accountManager.changePassword(newpassword);
		} catch (XMPPException e) {
			dumpStack("server exception changing password",e);
			throw new ECFException("server exception changing password",e);
		}
		return true;
	}
	public boolean createAccount(String username, String password, Map attributes) throws ECFException {
		if (accountManager == null) throw new ECFException("not connected");
		try {
			accountManager.createAccount(username,password,attributes);
		} catch (XMPPException e) {
			dumpStack("server exception creating account for "+username,e);
			throw new ECFException("server exception creating account for "+username,e);
		}
		return true;
	}
	public boolean deleteAccount() throws ECFException {
		if (accountManager == null) throw new ECFException("not connected");
		try {
			accountManager.deleteAccount();
		} catch (XMPPException e) {
			dumpStack("server exception deleting account",e);
			throw new ECFException("server exception deleting account",e);
		}
		return true;
	}
	public String getAccountInstructions() {
		if (accountManager == null) return null;
		return accountManager.getAccountInstructions();
	}
	public String[] getAccountAttributeNames() {
		if (accountManager == null) return null;
		Iterator i = accountManager.getAccountAttributes();
		List l = new ArrayList();
		for(; i.hasNext(); ) {
			l.add(i.next());
		}
		return (String []) l.toArray(new String[] {});
	}
	public Object getAccountAttribute(String name) {
		if (accountManager == null) return null;
		return accountManager.getAccountAttribute(name);
	}
	
	public boolean isAccountCreationSupported() {
		if (accountManager == null) return false;
		return accountManager.supportsAccountCreation();
	}

}
