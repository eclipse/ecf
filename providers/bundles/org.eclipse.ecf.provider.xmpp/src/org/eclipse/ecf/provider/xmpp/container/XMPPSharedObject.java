/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.xmpp.container;

import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.ISharedObjectConfig;
import org.eclipse.ecf.core.ISharedObjectContext;
import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.events.ISharedObjectContainerDepartedEvent;
import org.eclipse.ecf.core.events.ISharedObjectDeactivatedEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.provider.xmpp.Trace;

/**
 * @author slewis
 * 
 */
public class XMPPSharedObject implements ISharedObject {

	public static final String ROSTER_VIEW_ID = "org.eclipse.ecf.provider.xmpp.ui.view.rosterview";

	public static Trace trace = Trace.create("xmppsharedobject");

	ISharedObjectConfig config = null;

    /*
	XMPPConnection connection = null;
	IConfigViewer localUI = null;
	XMPPID localUserID = null;
	String localUserNickname = null;
	IMessageViewer messageViewer = null;
	IRosterViewer rosterViewer = null;
	IIMMessageSender sender = null;
	
	protected String canonicalizePresenceFrom(String from) {
		if (from == null)
			return null;
		int index = from.indexOf("/");
		if (index > 0) {
			return from.substring(0, index);
		} else
			return from;
	}
    */
	protected void debug(String msg) {
		if (Trace.ON && trace != null) {
			trace.msg(config.getSharedObjectID() + ":" + msg);
		}
	}

	protected void disconnect() {
		ISharedObjectContext context = getContext();
		if (context != null) {
			context.leaveGroup();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObject#dispose(org.eclipse.ecf.core.identity.ID)
	 */
    
	public void dispose(ID containerID) {
		config = null;
        /*
		sender = null;
		rosterViewer = null;
		messageViewer = null;
		localUI = null;
        */
	}

	protected void dumpStack(String msg, Throwable e) {
		if (Trace.ON && trace != null) {
			trace.dumpStack(e, config.getSharedObjectID() + ":" + msg);
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
    /*
	protected XMPPConnection getConnection() {
		return connection;
	}
	*/
	protected ISharedObjectContext getContext() {
		return config.getContext();
	}
	/*
	protected IMessageViewer getMessageViewer() {
		if (messageViewer != null)
			return messageViewer;
		else
			return new IMessageViewer() {
				public void showMessage(ID fromID, ID toID,
						IMessageViewer.Type type, String subject, String message) {
					debug("Received message from " + fromID + " to " + toID
							+ ", type=" + type + ", subject=" + subject
							+ ", message=" + message);
				}
			};
	}

	protected IRosterViewer getRosterViewer() {
		if (rosterViewer != null)
			return rosterViewer;
		else
			return new IRosterViewer() {

                public void receivePresence(ID userID, IPresence presence) {
                    debug("Received Presence Entry: "+presence+" for userID: "+userID);
                }
				public void receiveRosterEntry(IRosterEntry entry) {
					debug("Received Roster Entry: " + entry);
				}
			};
	}

	protected ITextInputHandler getTextInputHandler() {
		return new ITextInputHandler() {
			public void disconnect() {
				XMPPSharedObject.this.disconnect();
			}
			public void handleStartTyping(ID userID) {
			}
			public void handleTextLine(ID userID, String text) {
				sendIM(userID, text);
			}
		};
	}

	protected String getUserNameFromXMPPAddress(XMPPID userID) {
		return userID.getUsername();
	}
*/
	protected void handleContainerDepartedEvent(
			ISharedObjectContainerDepartedEvent event) {
		debug("Got container departed event: " + event);
        /*
		ID departedID = event.getDepartedContainerID();
		if (departedID != null) {
			// Notify UI that a member has departed (could be server)
			if (localUI != null) {
				localUI.memberDeparted(departedID);
			}
		}
        */
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
		debug("handleEvent("+event+")");
        /*
		if (event instanceof ISharedObjectActivatedEvent) {
			if (sender == null) {
				// get context
				ISharedObjectContext context = getContext();
				if (context != null) {
					Object adapter = context.getAdapter(IIMMessageSender.class);
					if (adapter != null) {
						sender = (IIMMessageSender) adapter;
					}
				}
			}
		} else if (event instanceof ISharedObjectContainerJoinedEvent) {
			handleJoin((ISharedObjectContainerJoinedEvent) event);
		} else if (event instanceof IQEvent) {
			handleIQEvent((IQEvent) event);
		} else if (event instanceof MessageEvent) {
			handleMessageEvent((MessageEvent) event);
		} else if (event instanceof PresenceEvent) {
			handlePresenceEvent((PresenceEvent) event);
		} else if (event instanceof ISharedObjectDeactivatedEvent) {
			handleDeactivatedEvent((ISharedObjectDeactivatedEvent) event);
		} else if (event instanceof ISharedObjectContainerDepartedEvent) {
			handleContainerDepartedEvent((ISharedObjectContainerDepartedEvent) event);
		} else if (event instanceof ISharedObjectMessageEvent) {
			handleSharedObjectMessageEvent((ISharedObjectMessageEvent)event);
		} else {
			debug("unrecognized event " + event);
		}
        */
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
	/*
	protected void handleIQEvent(IQEvent evt) {
		IQ iq = evt.getIQ();
		if (iq instanceof RosterPacket) {
			// Roster packet...report to UI
			RosterPacket rosterPacket = (RosterPacket) iq;
			for (Iterator i = rosterPacket.getRosterItems(); i.hasNext();) {
				IRosterEntry entry = makeRosterEntry((RosterPacket.Item) i
						.next());
				IRosterViewer rv = getRosterViewer();
				if (rv != null)
					rv.receiveRosterEntry(entry);
			}
		} else {
			debug("Received unknown IQ message: " + iq.toXML());
		}
	}

	protected void handleJoin(ISharedObjectContainerJoinedEvent event) {
		// show user interface
		showView();
	}

	protected void handleMessageEvent(MessageEvent evt) {
		Message msg = evt.getMessage();
		String from = msg.getFrom();
		String to = msg.getTo();
		String body = msg.getBody();
		String subject = msg.getSubject();
		ID fromID = makeIDFromName(canonicalizePresenceFrom(from));
		ID toID = makeIDFromName(canonicalizePresenceFrom(to));
		IMessageViewer mv = getMessageViewer();
		if (mv != null)
			mv.showMessage(fromID, toID, makeMessageType(msg.getType()),
					subject, body);
	}

	protected void handlePresenceEvent(PresenceEvent evt) {
		Presence xmppPresence = evt.getPresence();
		String from = canonicalizePresenceFrom(xmppPresence.getFrom());
		String to = xmppPresence.getTo();
		Mode mode = xmppPresence.getMode();
		Type type = xmppPresence.getType();
		int priority = xmppPresence.getPriority();
		String status = xmppPresence.getStatus();
		IPresence newPresence = makePresence(xmppPresence);
		ID fromID = makeIDFromName(from);
		IRosterViewer rv = getRosterViewer();
		if (rv != null)
			rv.receivePresence(fromID, newPresence);
	}

	protected void handleRoster(Roster roster) {
		for (Iterator i = roster.getEntries(); i.hasNext();) {
			IRosterEntry entry = makeRosterEntry((RosterEntry) i.next());
			IRosterViewer rv = getRosterViewer();
			if (rv != null)
				rv.receiveRosterEntry(entry);
		}
	}

	protected void handleSharedObjectMessageEvent(ISharedObjectMessageEvent event) {
		debug("shared object message event "+event);
		Object obj = event.getData();
		if (obj instanceof SharedObjectMsg) {
			SharedObjectMsg msg = (SharedObjectMsg) obj;
			String method = msg.getMsg();
			String param = msg.getParam();
			lookupAndInvoke(method,param);
		}
	}
    
	public void handleShowView(final String viewid) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				try {
                    IWorkbenchWindow ww = PlatformUI.getWorkbench()
                            .getActiveWorkbenchWindow();
                    IWorkbenchPage wp = ww.getActivePage();
                    IViewPart view = wp.showView(viewid);
				} catch (Exception e) {
					dumpStack("Exception showing view "+viewid, e);
				}
			}
		});
	}
	*/
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObject#init(org.eclipse.ecf.core.ISharedObjectConfig)
	 */
    
	public void init(ISharedObjectConfig initData)
			throws SharedObjectInitException {
		this.config = initData;
	}
    /*
	protected void interpret(ID target, String message) {
		StringTokenizer st = new StringTokenizer(message.substring(1),";");
		String first = st.nextToken();
		String second = st.nextToken();
		send(target,first,second);
	}
	protected void lookupAndInvoke(String method, String param) {
		try {
			Method m = getClass().getMethod(method,new Class[] { String.class });
			m.invoke(this, new Object[] { param });
		} catch (Exception e) {
			dumpStack("Exception in lookupAndInvoke",e);
		}
	}

	protected ID makeIDFromName(String name) {
		ID result = null;
		try {
			result = IDFactory.makeID(XMPPID.PROTOCOL, new Object[] { name });
			return result;
		} catch (Exception e) {
			dumpStack("Exception in makeIDFromName", e);
			return null;
		}
	}

	protected IRosterEntry.InterestType makeInterestType(
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

	protected IMessageViewer.Type makeMessageType(Message.Type type) {
		if (type == null)
			return IMessageViewer.Type.NORMAL;
		if (type == Message.Type.CHAT) {
			return IMessageViewer.Type.CHAT;
		} else if (type == Message.Type.NORMAL) {
			return IMessageViewer.Type.NORMAL;
		} else if (type == Message.Type.GROUP_CHAT) {
			return IMessageViewer.Type.GROUP_CHAT;
		} else if (type == Message.Type.HEADLINE) {
			return IMessageViewer.Type.HEADLINE;
		} else if (type == Message.Type.HEADLINE) {
			return IMessageViewer.Type.HEADLINE;
		} else
			return IMessageViewer.Type.NORMAL;
	}

	protected IPresence makePresence(Presence xmppPresence) {
		Mode mode = xmppPresence.getMode();
		Type type = xmppPresence.getType();
		int priority = xmppPresence.getPriority();
		String status = xmppPresence.getStatus();
		IPresence newPresence = new org.eclipse.ecf.ui.presence.Presence(
				makePresenceType(xmppPresence), priority, status, makePresenceMode(xmppPresence));
		return newPresence;
	}

	protected IPresence.Mode makePresenceMode(Presence xmppPresence) {
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

	protected IPresence.Type makePresenceType(Presence xmppPresence) {
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

	protected IRosterEntry makeRosterEntry(RosterEntry entry) {
		try {
			ID userID = makeIDFromName(entry.getUser());
			String name = entry.getName();
			RosterPacket.ItemType itemType = entry.getType();
			IRosterEntry.InterestType iType = makeInterestType(itemType);
			IRosterEntry newEntry = new org.eclipse.ecf.ui.presence.RosterEntry(
					userID, name, iType);
			Iterator grps = entry.getGroups();
			for (; grps.hasNext();) {
				RosterGroup grp = (RosterGroup) grps.next();
				IRosterGroup localGrp = makeRosterGroup(grp);
				newEntry.add(localGrp);
			}
			return newEntry;
		} catch (Exception e) {
			dumpStack("Exception in makeRosterEntry", e);
		}
		return null;
	}

	protected IRosterEntry makeRosterEntry(RosterPacket.Item entry) {
		try {
			ID userID = makeIDFromName(entry.getUser());
			String name = entry.getName();
			RosterPacket.ItemType itemType = entry.getItemType();
			IRosterEntry.InterestType iType = makeInterestType(itemType);
			IRosterEntry newEntry = new org.eclipse.ecf.ui.presence.RosterEntry(
					userID, name, iType);
			Iterator grps = entry.getGroupNames();
			for (; grps.hasNext();) {
				String grp = (String) grps.next();
				IRosterGroup localGrp = makeRosterGroup(grp);
				newEntry.add(localGrp);
			}
			return newEntry;
		} catch (Exception e) {
			dumpStack("Exception in makeRosterEntry", e);
		}
		return null;
	}

	protected IRosterGroup makeRosterGroup(RosterGroup grp) {
		return new org.eclipse.ecf.ui.presence.RosterGroup(grp.getName());
	}

	protected IRosterGroup makeRosterGroup(String grp) {
		return new org.eclipse.ecf.ui.presence.RosterGroup(grp);
	}
	*/
	/*
	 * Protocol for launching remote views
	 */
	public void send(ID target, String cmd, String param) {
		try {
			SharedObjectMsg m = new SharedObjectMsg(cmd,param);
			getContext().sendMessage(target,m);
		} catch (Exception e) {
			dumpStack("Exception on send() to "
					+ target,e);
		}
	}
    /*
	protected void sendIM(ID target, String message) {
		try {
			if (message.startsWith(";")) {
				interpret(target,message);
			} else sender.sendMessage(target, message);
		} catch (Exception e) {
			dumpStack("Exception in sendMessage ", e);
		}
	}
	public void sendShowView(ID target, String viewid) {
		send(target,"handleShowView",viewid);
	}

	protected void setConnection(XMPPConnection connection) {
		this.connection = connection;
	}
	protected void setupUI(IViewPart view) {
		rosterViewer = (IRosterViewer) view.getAdapter(IRosterViewer.class);
		messageViewer = (IMessageViewer) view.getAdapter(IMessageViewer.class);
		localUI = (IConfigViewer) view
				.getAdapter(IConfigViewer.class);
		localUI.setLocalUser(new User(localUserID, localUserNickname),
				getTextInputHandler());
		localUI.setGroup(getContext().getGroupID());
	}

	protected void setUserID(ID userID, String nickname) {
		this.localUserID = (XMPPID) userID;
		if (nickname == null) {
			nickname = getUserNameFromXMPPAddress(localUserID);
		}
		this.localUserNickname = nickname;
	}

	protected void showView() {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                try {
                    IWorkbenchWindow ww = PlatformUI.getWorkbench()
                            .getActiveWorkbenchWindow();
                    IWorkbenchPage wp = ww.getActivePage();
                    IViewPart view = wp.showView(ROSTER_VIEW_ID);
                    setupUI(view);
                } catch (Exception e) {
                    dumpStack("Exception showing view", e);
                }
            }
        });
    }
    */
}
