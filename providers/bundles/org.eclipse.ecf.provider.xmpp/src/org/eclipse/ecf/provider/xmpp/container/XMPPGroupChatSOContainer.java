/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.xmpp.container;

import java.io.IOException;
import java.util.HashMap;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.ISharedObjectContainerConfig;
import org.eclipse.ecf.core.SharedObjectAddException;
import org.eclipse.ecf.core.comm.AsynchConnectionEvent;
import org.eclipse.ecf.core.comm.ConnectionInstantiationException;
import org.eclipse.ecf.core.comm.ISynchAsynchConnection;
import org.eclipse.ecf.core.events.SharedObjectContainerConnectedEvent;
import org.eclipse.ecf.core.events.SharedObjectContainerConnectingEvent;
import org.eclipse.ecf.core.events.SharedObjectContainerDisconnectedEvent;
import org.eclipse.ecf.core.events.SharedObjectContainerDisconnectingEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.IDInstantiationException;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.IQueueEnqueue;
import org.eclipse.ecf.presence.IMessageListener;
import org.eclipse.ecf.presence.IMessageSender;
import org.eclipse.ecf.presence.IMessageListener.Type;
import org.eclipse.ecf.presence.chat.IChatRoomContainer;
import org.eclipse.ecf.provider.generic.ClientSOContainer;
import org.eclipse.ecf.provider.generic.ContainerMessage;
import org.eclipse.ecf.provider.generic.SOConfig;
import org.eclipse.ecf.provider.generic.SOContainerConfig;
import org.eclipse.ecf.provider.generic.SOContext;
import org.eclipse.ecf.provider.generic.SOWrapper;
import org.eclipse.ecf.provider.xmpp.XmppPlugin;
import org.eclipse.ecf.provider.xmpp.events.IQEvent;
import org.eclipse.ecf.provider.xmpp.events.MessageEvent;
import org.eclipse.ecf.provider.xmpp.events.PresenceEvent;
import org.eclipse.ecf.provider.xmpp.identity.XMPPRoomID;
import org.eclipse.ecf.provider.xmpp.smack.ECFConnectionObjectPacketEvent;
import org.eclipse.ecf.provider.xmpp.smack.ECFConnectionPacketEvent;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.MultiUserChat;

public class XMPPGroupChatSOContainer extends ClientSOContainer implements IChatRoomContainer {
    public static final String XMPP_GROUP_CHAT_SHARED_OBJECT_ID = XMPPClientSOContainer.class
            .getName()
            + ".xmppgroupchathandler";
    XMPPConnection connection;
    ID sharedObjectID;
    XMPPGroupChatSharedObject sharedObject;
    MultiUserChat multiuserchat;
    ISharedObjectContainerConfig config;
    Namespace usernamespace = null;
    
    public XMPPGroupChatSOContainer(ISharedObjectContainerConfig config,
            XMPPConnection conn, Namespace usernamespace) throws IDInstantiationException {
        super(config);
        this.connection = conn;
        this.config = config;
        this.usernamespace = usernamespace;
		initializeSharedObject();
    }
    public XMPPGroupChatSOContainer(XMPPConnection conn, Namespace usernamespace) throws IDInstantiationException {
    	this(new SOContainerConfig(IDFactory.getDefault().makeGUID()),conn,usernamespace);
    }
    public void dispose() {
        disconnect();
        getSharedObjectManager().removeSharedObject(sharedObjectID);
        if (sharedObject != null) {
        	sharedObject.dispose(getID());
        }
        super.dispose();
    }

    protected void handleChatMessage(Message mess) throws IOException {
        SOWrapper wrap = getSharedObjectWrapper(sharedObjectID);
        if (wrap != null) {
            wrap.deliverEvent(new MessageEvent(mess));
        }
    }

    protected void handleContainerMessage(ContainerMessage mess)
            throws IOException {
        if (mess == null) {
            debug("got null container message...ignoring");
            return;
        }
        Object data = mess.getData();
        if (data instanceof ContainerMessage.CreateMessage) {
            handleCreateMessage(mess);
        } else if (data instanceof ContainerMessage.CreateResponseMessage) {
            handleCreateResponseMessage(mess);
        } else if (data instanceof ContainerMessage.SharedObjectMessage) {
            handleSharedObjectMessage(mess);
        } else if (data instanceof ContainerMessage.SharedObjectDisposeMessage) {
            handleSharedObjectDisposeMessage(mess);
        } else {
            debug("got unrecognized container message...ignoring: " + mess);
        }
    }

    protected void handleIQMessage(IQ mess) throws IOException {
        SOWrapper wrap = getSharedObjectWrapper(sharedObjectID);
        if (wrap != null) {
            wrap.deliverEvent(new IQEvent(mess));
        }
    }

    protected void handlePresenceMessage(Presence mess) throws IOException {
        SOWrapper wrap = getSharedObjectWrapper(sharedObjectID);
        if (wrap != null) {
            wrap.deliverEvent(new PresenceEvent(mess));
        }
    }

    protected void handleXMPPMessage(Packet aPacket) throws IOException {
        if (aPacket instanceof IQ) {
            handleIQMessage((IQ) aPacket);
        } else if (aPacket instanceof Message) {
            handleChatMessage((Message) aPacket);
        } else if (aPacket instanceof Presence) {
            handlePresenceMessage((Presence) aPacket);
        } else {
            // unexpected message
            debug("got unexpected packet " + aPacket);
        }
    }

    protected void initializeSharedObject() throws IDInstantiationException {
        sharedObjectID = IDFactory.getDefault().makeStringID(XMPP_GROUP_CHAT_SHARED_OBJECT_ID);
        sharedObject = new XMPPGroupChatSharedObject(usernamespace);
    }

    protected void addSharedObjectToContainer(ID remote)
            throws SharedObjectAddException {
        getSharedObjectManager().addSharedObject(sharedObjectID, sharedObject,
                new HashMap());
    }

    protected void cleanUpConnectFail() {
        if (sharedObject != null) {
            getSharedObjectManager().removeSharedObject(sharedObjectID);
            sharedObject = null;
            sharedObjectID = null;
        }
		connectionState = UNCONNECTED;
		remoteServerID = null;
    }

    public Namespace getConnectNamespace() {
    	return IDFactory.getDefault().getNamespaceByName(XmppPlugin.getDefault().getRoomNamespaceIdentifier());
    }
	public void connect(ID remote, IConnectContext joinContext)
	throws ContainerConnectException {
    	if (!(remote instanceof XMPPRoomID)) {
    		throw new ContainerConnectException("remote "+remote+" is not of room id type");
    	}
    	XMPPRoomID roomID = (XMPPRoomID) remote;
		fireContainerEvent(new SharedObjectContainerConnectingEvent(
				this.getID(), remote, joinContext));
		synchronized (getConnectLock()) {
	        try {
	    		connectionState = CONNECTING;
	    		remoteServerID = null;
	    		
	            addSharedObjectToContainer(remote);
	            multiuserchat = new MultiUserChat(connection,roomID.getMucString());
	            String nickname = roomID.getNickname();
	            //multiuserchat.create(nickname);
	            //multiuserchat.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));
	            multiuserchat.addMessageListener(new PacketListener() {
	    			public void processPacket(Packet arg0) {
						try {
	    					handleXMPPMessage(arg0);
	    				} catch (IOException e) {
	    					logException("Exception in handleXMPPMessage",e);
	    				}
	    			}
	            	
	            });
	            multiuserchat.join(nickname);
	    		connectionState = CONNECTED;
	    		remoteServerID = roomID;
				fireContainerEvent(new SharedObjectContainerConnectedEvent(this
						.getID(), roomID));
	        } catch (Exception e) {
	            cleanUpConnectFail();
	            ContainerConnectException ce = new ContainerConnectException("Exception joining "+roomID);
	            ce.setStackTrace(e.getStackTrace());
	            throw ce;
	        } 
	    }
    }

    public void disconnect() {
        ID groupID = getConnectedID();
        fireContainerEvent(new SharedObjectContainerDisconnectingEvent(this
                .getID(), groupID));
        synchronized (getConnectLock()) {
            // If we are currently connected
            if (isConnected()) {
            	multiuserchat.leave();
            }
            connectionState = UNCONNECTED;
            this.connection = null;
            remoteServerID = null;
        }
        // notify listeners
        fireContainerEvent(new SharedObjectContainerDisconnectedEvent(this.getID(),
                groupID));
    }

    protected SOContext makeSharedObjectContext(SOConfig soconfig,
            IQueueEnqueue queue) {
        return new XMPPContainerContext(soconfig.getSharedObjectID(), soconfig
                .getHomeContainerID(), this, soconfig.getProperties(), queue);
    }

    protected void processAsynch(AsynchConnectionEvent e) {
        try {
            if (e instanceof ECFConnectionPacketEvent) {
                // It's a regular message...just print for now
                Packet chatMess = (Packet) e.getData();
                handleXMPPMessage(chatMess);
                return;
            } else if (e instanceof ECFConnectionObjectPacketEvent) {
                ECFConnectionObjectPacketEvent evt = (ECFConnectionObjectPacketEvent) e;
                Object obj = evt.getObjectValue();
                // this should be a ContainerMessage
                Object cm = deserializeContainerMessage((byte[]) obj);
                if (cm == null)
                    throw new IOException("deserialized object is null");
                ContainerMessage contMessage = (ContainerMessage) cm;
                handleContainerMessage(contMessage);
            } else {
                // Unexpected type...
                debug("got unexpected event: " + e);
            }
        } catch (Exception except) {
            System.err.println("Exception in processAsynch");
            except.printStackTrace(System.err);
            dumpStack("Exception processing event " + e, except);
        }
    }

	protected ID makeChatRoomID(String groupName) throws IDInstantiationException {
		String username = connection.getUser();
		int atIndex = username.indexOf('@');
		if (atIndex > 0) username = username.substring(0,atIndex);
		String host = connection.getHost();
		Namespace ns = getConnectNamespace();
		ID targetID = IDFactory.getDefault().makeID(ns,new Object[] { username, host, null, groupName, username });
		return targetID;
	}
    protected ISynchAsynchConnection makeConnection(ID remoteSpace,
            Object data) throws ConnectionInstantiationException {
        return null;
    }
	public void addMessageListener(IMessageListener listener) {
		if (sharedObject != null) {
			sharedObject.addMessageListener(listener);
		}
	}
	public IMessageSender getMessageSender() {
		return new IMessageSender() {
			public void sendMessage(ID fromID, ID toID, Type type, String subject, String messageBody) {
				if (multiuserchat != null) {
					try {
						multiuserchat.sendMessage(messageBody);
					} catch (XMPPException e) {
						// TODO log
						e.printStackTrace();
					}
				}
			}
		};
	}
	public void connect(String groupName) throws ContainerConnectException {
		ID targetID = null;
		try {
			targetID = makeChatRoomID(groupName);
		} catch (IDInstantiationException e) {
			ContainerConnectException newExcept = new ContainerConnectException("Exception creating chat room id",e);
			newExcept.setStackTrace(e.getStackTrace());
			throw newExcept;
		}
		this.connect(targetID,null);
	}
}