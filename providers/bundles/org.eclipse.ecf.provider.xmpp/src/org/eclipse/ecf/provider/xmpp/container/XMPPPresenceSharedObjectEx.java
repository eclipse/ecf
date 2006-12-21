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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.sharedobject.ISharedObject;
import org.eclipse.ecf.core.sharedobject.ISharedObjectConfig;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContext;
import org.eclipse.ecf.core.sharedobject.SharedObjectInitException;
import org.eclipse.ecf.core.sharedobject.events.ISharedObjectActivatedEvent;
import org.eclipse.ecf.core.sharedobject.events.ISharedObjectDeactivatedEvent;
import org.eclipse.ecf.core.sharedobject.events.ISharedObjectMessageEvent;
import org.eclipse.ecf.core.sharedobject.events.ISharedObjectMessageListener;
import org.eclipse.ecf.core.user.IUser;
import org.eclipse.ecf.core.user.User;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.presence.IAccountManager;
import org.eclipse.ecf.presence.IMessageListener;
import org.eclipse.ecf.presence.IPresenceListener;
import org.eclipse.ecf.presence.IRosterSubscriptionListener;
import org.eclipse.ecf.presence.chat.IInvitationListener;
import org.eclipse.ecf.presence.roster.AbstractRosterManager;
import org.eclipse.ecf.presence.roster.IPresence;
import org.eclipse.ecf.presence.roster.IPresenceSender;
import org.eclipse.ecf.presence.roster.IRosterGroup;
import org.eclipse.ecf.presence.roster.IRosterItem;
import org.eclipse.ecf.presence.roster.IRosterManager;
import org.eclipse.ecf.presence.roster.IRosterSubscriptionSender;
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
import org.jivesoftware.smack.packet.RosterPacket.Item;

public class XMPPPresenceSharedObjectEx implements ISharedObject,
		IAccountManager {

	ISharedObjectConfig config = null;

	XMPPConnection connection = null;

	AccountManager accountManager = null;

	Vector messageListeners = new Vector();

	Vector sharedObjectMessageListeners = new Vector();

	Vector invitationListeners = new Vector();

	Namespace namespace = null;

	XMPPClientSOContainer container = null;

	public XMPPPresenceSharedObjectEx(XMPPClientSOContainer container) {
		this.container = container;
	}

	protected void fireInvitationReceived(ID roomID, ID fromID, ID toID,
			String subject, String body) {
		for (Iterator i = invitationListeners.iterator(); i.hasNext();) {
			IInvitationListener l = (IInvitationListener) i.next();
			l.handleInvitationReceived(roomID, fromID, subject, body);
		}
	}

	protected void addInvitationListener(IInvitationListener listener) {
		invitationListeners.add(listener);
	}

	protected void removeInvitationListener(IInvitationListener listener) {
		invitationListeners.remove(listener);
	}

	protected void addMessageListener(IMessageListener listener) {
		messageListeners.add(listener);
	}

	protected void removeMessageListener(IMessageListener listener) {
		messageListeners.add(listener);
	}

	protected void addSharedObjectMessageListener(
			ISharedObjectMessageListener listener) {
		sharedObjectMessageListeners.add(listener);
	}

	protected void removeSharedObjectMessageListener(
			ISharedObjectMessageListener listener) {
		sharedObjectMessageListeners.remove(listener);
	}

	protected String canonicalizePresenceFrom(String from) {
		if (from == null)
			return null;
		else
			return from;
		/*
		 * int index = from.indexOf("/"); if (index > 0) { return
		 * from.substring(0, index); } else return from;
		 */
	}

	protected void debug(String msg) {
		System.out.println(config.getSharedObjectID() + ":" + msg);
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
		e.printStackTrace(System.err);
	}

	protected void fireMessage(ID from, ID to, IMessageListener.Type type,
			String subject, String body) {
		for (Iterator i = messageListeners.iterator(); i.hasNext();) {
			IMessageListener l = (IMessageListener) i.next();
			l.handleMessage(from, to, type, subject, body);
		}
	}

	private void addToRoster(IRosterItem[] items) {
		for (int i = 0; i < items.length; i++)
			roster.addItem(items[i]);
		rosterManager.notifyRosterUpdate(null);
	}

	protected void fireSharedObjectMessage(ISharedObjectMessageEvent event) {
		for (Iterator i = sharedObjectMessageListeners.iterator(); i.hasNext();) {
			ISharedObjectMessageListener l = (ISharedObjectMessageListener) i
					.next();
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
		} else if (event instanceof ISharedObjectMessageEvent) {
			fireSharedObjectMessage((ISharedObjectMessageEvent) event);
		} else {
			debug("unrecognized event " + event);
		}
	}

	protected ID createRoomIDFromName(String from) {
		try {
			return new XMPPRoomID(namespace, connection, from);
		} catch (URISyntaxException e) {
			dumpStack("Exception in createRoomIDFromName", e);
			return null;
		}
	}

	protected ID createUserIDFromName(String name) {
		ID result = null;
		try {
			result = new XMPPID(namespace, name);
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
			fireInvitationReceived(roomID, fromID, toID, subject, body);
		} else {
			debug("got invitation event for other connection " + event);
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
					RosterPacket.Item item = (RosterPacket.Item) i.next();
					RosterPacket.ItemType itemType = item.getItemType();
					if (itemType == RosterPacket.ItemType.NONE)
						removeItemFromRoster(roster.getItems(),
								createIDFromName(item.getUser()));
					else
						addToRoster(createRosterEntry(item));
				}
			}
		} else {
			debug("Received non rosterpacket IQ message: " + iq.toXML());
		}
	}

	private void removeFromRoster(Item item) {
	}

	private void removeItemFromRoster(Collection rosterItems,
			XMPPID itemIDToRemove) {
		synchronized (rosterItems) {
			for (Iterator i = rosterItems.iterator(); i.hasNext();) {
				IRosterItem item = (IRosterItem) i.next();
				if (item instanceof org.eclipse.ecf.presence.roster.RosterGroup) {
					org.eclipse.ecf.presence.roster.RosterGroup group = (org.eclipse.ecf.presence.roster.RosterGroup) item;
					removeItemFromRosterGroup(group, itemIDToRemove);
					if (group.getEntries().size() == 0)
						roster.removeItem(item);
					rosterManager.notifyRosterUpdate(null);
				} else if (item instanceof org.eclipse.ecf.presence.roster.RosterEntry) {
					if (((org.eclipse.ecf.presence.roster.RosterEntry) item)
							.getUser().getID().equals(itemIDToRemove))
						roster.removeItem(item);
				}
			}
		}
	}

	private void removeItemFromRosterGroup(
			org.eclipse.ecf.presence.roster.RosterGroup group,
			XMPPID itemIDToRemove) {
		for (Iterator i = group.getEntries().iterator(); i.hasNext();) {
			org.eclipse.ecf.presence.roster.RosterEntry entry = (org.eclipse.ecf.presence.roster.RosterEntry) i
					.next();
			if (entry.getUser().getID().equals(itemIDToRemove))
				group.remove(entry);
		}
	}

	protected Message.Type[] ALLOWED_MESSAGES = { Message.Type.CHAT,
			Message.Type.ERROR, Message.Type.HEADLINE, Message.Type.NORMAL };

	protected Message filterMessageType(Message msg) {
		for (int i = 0; i < ALLOWED_MESSAGES.length; i++) {
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
		if (msg != null)
			fireMessage(fromID, toID, createMessageType(msg.getType()),
					subject, body);
	}

	protected void handlePresenceEvent(PresenceEvent evt) {
		Presence xmppPresence = evt.getPresence();
		String from = canonicalizePresenceFrom(xmppPresence.getFrom());
		IPresence newPresence = createIPresence(xmppPresence);
		XMPPID fromID = createIDFromName(from);
		if (newPresence.getType().equals(IPresence.Type.SUBSCRIBE)
				|| newPresence.getType().equals(IPresence.Type.UNSUBSCRIBE)
				|| newPresence.getType().equals(IPresence.Type.SUBSCRIBED)
				|| newPresence.getType().equals(IPresence.Type.UNSUBSCRIBED)) {
			rosterManager.notifySubscriptionListener(fromID, newPresence);
		} else {
			updatePresence(fromID, newPresence);
			rosterManager.notifyRosterUpdate(null);
		}
	}

	private void updatePresence(XMPPID fromID, IPresence newPresence) {
		for (Iterator i = roster.getItems().iterator(); i.hasNext();) {
			IRosterItem item = (IRosterItem) i.next();
			if (item instanceof IRosterGroup)
				updatePresenceInGroup((IRosterGroup) item, fromID, newPresence);
			else if (item instanceof org.eclipse.ecf.presence.roster.RosterEntry)
				updatePresenceForMatchingEntry(
						(org.eclipse.ecf.presence.roster.RosterEntry) item,
						fromID, newPresence);
		}
	}

	private void updatePresenceForMatchingEntry(
			org.eclipse.ecf.presence.roster.RosterEntry entry, XMPPID fromID,
			IPresence newPresence) {
		User user = (User) entry.getUser();
		if (fromID.equals(user.getID()))
			entry.setPresence(newPresence);
	}

	private void updatePresenceInGroup(IRosterGroup group, XMPPID fromID,
			IPresence newPresence) {
		for (Iterator i = group.getEntries().iterator(); i.hasNext();) {
			updatePresenceForMatchingEntry(
					(org.eclipse.ecf.presence.roster.RosterEntry) i.next(),
					fromID, newPresence);
		}
	}

	protected void handleRoster(Roster roster) {
		for (Iterator i = roster.getEntries(); i.hasNext();) {
			IRosterItem[] items = createRosterEntry((RosterEntry) i.next());
			synchronized (roster) {
				for (int j = 0; j < items.length; j++) {
					this.roster.addItem(items[j]);
				}
			}
		}
		rosterManager.notifyRosterUpdate(null);
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

	protected XMPPID createIDFromName(String name) {
		try {
			return new XMPPID(namespace, name);
		} catch (Exception e) {
			dumpStack("Exception in createIDFromName", e);
			return null;
		}
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
			return IMessageListener.Type.SYSTEM;
		} else if (type == Message.Type.HEADLINE) {
			return IMessageListener.Type.SYSTEM;
		} else
			return IMessageListener.Type.NORMAL;
	}

	protected IPresence createIPresence(Presence xmppPresence) {
		String status = xmppPresence.getStatus();
		IPresence newPresence = new org.eclipse.ecf.presence.roster.Presence(
				createIPresenceType(xmppPresence), status,
				createIPresenceMode(xmppPresence));
		return newPresence;
	}

	protected Presence createPresence(IPresence ipresence) {
		String status = ipresence.getStatus();
		Presence newPresence = new Presence(createPresenceType(ipresence),
				status, 0, createPresenceMode(ipresence));
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

	protected IRosterItem[] createRosterEntry(RosterEntry entry) {
		return createRosterEntry(entry.getGroups(), roster, new User(
				createIDFromName(entry.getUser()), entry.getName()));
	}

	private IRosterItem[] createRosterEntry(Iterator grps, Object parent,
			IUser user) {
		List result = new ArrayList();
		if (grps.hasNext()) {
			for (; grps.hasNext();) {
				Object o = grps.next();
				// Get group name
				String groupName = (o instanceof String) ? (String) o
						: ((RosterGroup) o).getName();

				if (groupName == null || groupName.equals("")) {
					createRosterEntry(result, parent, user);
					continue;
				}
				// See if group is already in roster
				org.eclipse.ecf.presence.roster.RosterGroup rosterGroup = findRosterGroup(
						parent, groupName);
				// Set flag if not
				boolean groupFound = rosterGroup != null;
				if (!groupFound)
					rosterGroup = new org.eclipse.ecf.presence.roster.RosterGroup(
							parent, groupName);

				org.eclipse.ecf.presence.roster.RosterEntry oldEntry = findRosterEntry(
						rosterGroup, user);
				// Now create new roster entry
				new org.eclipse.ecf.presence.roster.RosterEntry(rosterGroup,
						user, new org.eclipse.ecf.presence.roster.Presence(
								IPresence.Type.UNAVAILABLE,
								IPresence.Type.UNAVAILABLE.toString(),
								IPresence.Mode.AWAY));
				// Only add localGrp if not already in list
				if (!groupFound)
					result.add(rosterGroup);
			}
		} else
			createRosterEntry(result, parent, user);
		return (IRosterItem[]) result.toArray(new IRosterItem[] {});
	}

	private void createRosterEntry(List result, Object parent, IUser user) {
		org.eclipse.ecf.presence.roster.RosterEntry oldEntry = findRosterEntry(
				(org.eclipse.ecf.presence.roster.RosterGroup) null, user);
		if (oldEntry == null) {
			org.eclipse.ecf.presence.roster.RosterEntry newEntry = new org.eclipse.ecf.presence.roster.RosterEntry(
					parent, user, new org.eclipse.ecf.presence.roster.Presence(
							IPresence.Type.UNAVAILABLE,
							IPresence.Type.UNAVAILABLE.toString(),
							IPresence.Mode.AWAY));
			result.add(newEntry);
		}
	}

	private org.eclipse.ecf.presence.roster.RosterEntry findRosterEntry(
			org.eclipse.ecf.presence.roster.RosterGroup rosterGroup, IUser user) {
		if (rosterGroup != null)
			return findRosterEntry(rosterGroup.getEntries(), user);
		else
			return findRosterEntry(roster.getItems(), user);
	}

	private org.eclipse.ecf.presence.roster.RosterEntry findRosterEntry(
			Collection entries, IUser user) {
		for (Iterator i = entries.iterator(); i.hasNext();) {
			Object o = i.next();
			if (o instanceof org.eclipse.ecf.presence.roster.RosterEntry) {
				org.eclipse.ecf.presence.roster.RosterEntry entry = (org.eclipse.ecf.presence.roster.RosterEntry) o;
				if (entry.getUser().getID().equals(user.getID()))
					return entry;
			}
		}
		return null;
	}

	protected IRosterItem[] createRosterEntry(RosterPacket.Item entry) {
		XMPPID id = createIDFromName(entry.getUser());
		String name = entry.getName();
		if (name == null)
			name = id.getUsername();
		return createRosterEntry(entry.getGroupNames(), roster, new User(id,
				name));
	}

	protected org.eclipse.ecf.presence.roster.RosterGroup findRosterGroup(
			Object parent, String grp) {
		Collection items = roster.getItems();
		for (Iterator i = items.iterator(); i.hasNext();) {
			IRosterItem item = (IRosterItem) i.next();
			if (item.getName().equals(grp))
				return (org.eclipse.ecf.presence.roster.RosterGroup) item;
		}
		return null;
	}

	// return new org.eclipse.ecf.presence.roster.RosterGroup(parent, grp);

	protected void setConnection(XMPPConnection connection) {
		this.connection = connection;
		if (connection != null) {
			accountManager = new AccountManager(connection);
		}
	}

	public boolean changePassword(String newpassword) throws ECFException {
		if (accountManager == null)
			throw new ECFException("not connected");
		try {
			accountManager.changePassword(newpassword);
		} catch (XMPPException e) {
			dumpStack("server exception changing password", e);
			throw new ECFException("server exception changing password", e);
		}
		return true;
	}

	public boolean createAccount(String username, String password,
			Map attributes) throws ECFException {
		if (accountManager == null)
			throw new ECFException("not connected");
		try {
			accountManager.createAccount(username, password, attributes);
		} catch (XMPPException e) {
			dumpStack("server exception creating account for " + username, e);
			throw new ECFException("server exception creating account for "
					+ username, e);
		}
		return true;
	}

	public boolean deleteAccount() throws ECFException {
		if (accountManager == null)
			throw new ECFException("not connected");
		try {
			accountManager.deleteAccount();
		} catch (XMPPException e) {
			dumpStack("server exception deleting account", e);
			throw new ECFException("server exception deleting account", e);
		}
		return true;
	}

	public String getAccountCreationInstructions() {
		if (accountManager == null)
			return null;
		return accountManager.getAccountInstructions();
	}

	public String[] getAccountAttributeNames() {
		if (accountManager == null)
			return null;
		Iterator i = accountManager.getAccountAttributes();
		List l = new ArrayList();
		for (; i.hasNext();) {
			l.add(i.next());
		}
		return (String[]) l.toArray(new String[] {});
	}

	public Object getAccountAttribute(String name) {
		if (accountManager == null)
			return null;
		return accountManager.getAccountAttribute(name);
	}

	public boolean isAccountCreationSupported() {
		if (accountManager == null)
			return false;
		return accountManager.supportsAccountCreation();
	}

	public IRosterManager getRosterManager() {
		return rosterManager;
	}

	public void setUser(IUser user) {
		rosterManager.setUser(user);
	}

	protected org.eclipse.ecf.presence.roster.Roster roster = new org.eclipse.ecf.presence.roster.Roster();

	protected PresenceRosterManager rosterManager = new PresenceRosterManager(
			roster);

	class PresenceRosterManager extends AbstractRosterManager {

		public PresenceRosterManager(
				org.eclipse.ecf.presence.roster.Roster roster) {
			super(roster);
		}

		public void notifySubscriptionListener(ID fromID, IPresence presence) {
			this.fireSubscriptionListener(fromID, presence);
		}

		public void notifyRosterUpdate(IRosterItem changedItem) {
			fireRosterUpdate(changedItem);
		}

		public void setUser(IUser user) {
			((org.eclipse.ecf.presence.roster.Roster) getRoster())
					.setUser(user);
			notifyRosterUpdate(null);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ecf.presence.roster.AbstractRosterManager#getPresenceSender()
		 */
		public IPresenceSender getPresenceSender() {
			return container.getPresenceSender();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ecf.presence.roster.AbstractRosterManager#getRosterSubscriptionSender()
		 */
		public IRosterSubscriptionSender getRosterSubscriptionSender() {
			return container.getRosterSubscriptionSender();
		}

	}

	public Presence createPresence(org.eclipse.ecf.presence.IPresence presence) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addPresenceListener(IPresenceListener listener) {
		// TODO Auto-generated method stub

	}

	public void addSubscribeListener(IRosterSubscriptionListener listener) {
		// TODO Auto-generated method stub

	}

	public void removePresenceListener(IPresenceListener listener) {
		// TODO Auto-generated method stub

	}

	public void removeSubscribeListener(IRosterSubscriptionListener listener) {
		// TODO Auto-generated method stub

	}

}
