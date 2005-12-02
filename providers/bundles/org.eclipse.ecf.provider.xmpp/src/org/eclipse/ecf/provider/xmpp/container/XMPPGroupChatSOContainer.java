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
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
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
import org.eclipse.ecf.presence.IInvitationListener;
import org.eclipse.ecf.presence.IMessageListener;
import org.eclipse.ecf.presence.chat.IChatMessageSender;
import org.eclipse.ecf.presence.chat.IChatParticipantListener;
import org.eclipse.ecf.presence.chat.IChatRoomContainer;
import org.eclipse.ecf.provider.generic.ClientSOContainer;
import org.eclipse.ecf.provider.generic.ContainerMessage;
import org.eclipse.ecf.provider.generic.SOConfig;
import org.eclipse.ecf.provider.generic.SOContainerConfig;
import org.eclipse.ecf.provider.generic.SOContext;
import org.eclipse.ecf.provider.generic.SOWrapper;
import org.eclipse.ecf.provider.xmpp.XmppPlugin;
import org.eclipse.ecf.provider.xmpp.events.ChatMembershipEvent;
import org.eclipse.ecf.provider.xmpp.events.IQEvent;
import org.eclipse.ecf.provider.xmpp.events.InvitationReceivedEvent;
import org.eclipse.ecf.provider.xmpp.events.MessageEvent;
import org.eclipse.ecf.provider.xmpp.events.PresenceEvent;
import org.eclipse.ecf.provider.xmpp.identity.XMPPRoomID;
import org.eclipse.ecf.provider.xmpp.smack.ECFConnectionObjectPacketEvent;
import org.eclipse.ecf.provider.xmpp.smack.ECFConnectionPacketEvent;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.InvitationRejectionListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;

public class XMPPGroupChatSOContainer extends ClientSOContainer implements IChatRoomContainer {
    public static final String XMPP_GROUP_CHAT_SHARED_OBJECT_ID = XMPPClientSOContainer.class
            .getName()
            + ".xmppgroupchathandler";
    XMPPConnection xmppconnection;
    ID sharedObjectID;
    XMPPGroupChatSharedObject sharedObject;
    MultiUserChat multiuserchat;
    ISharedObjectContainerConfig config;
    Namespace usernamespace = null;
    XMPPRoomID roomID = null;
    
    public XMPPGroupChatSOContainer(ISharedObjectContainerConfig config, ISynchAsynchConnection conn,
            XMPPConnection xmppconn, Namespace usernamespace) throws IDInstantiationException {
        super(config);
        this.connection = conn;
        this.xmppconnection = xmppconn;
        this.config = config;
        this.usernamespace = usernamespace;
		initializeSharedObject();
    }
    public XMPPGroupChatSOContainer(ISynchAsynchConnection conn, XMPPConnection xmppconn, Namespace usernamespace) throws IDInstantiationException {
    	this(new SOContainerConfig(IDFactory.getDefault().makeGUID()),conn, xmppconn,usernamespace);
    }
    public void dispose() {
        disconnect();
        if (sharedObjectID != null) {
        	getSharedObjectManager().removeSharedObject(sharedObjectID);
        	sharedObjectID = null;
        }
        if (sharedObject != null) {
        	sharedObject.dispose(getID());
        	sharedObject = null;
        }
        super.dispose();
    }

	protected void sendMessage(ContainerMessage data) throws IOException {
		synchronized (getConnectLock()) {
			ID toID = data.getToContainerID();
			if (toID == null) {
				data.setToContainerID(remoteServerID);
			}
			super.sendMessage(data);
		}
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
    protected void handleChatMembershipEvent(String from, boolean add) {
        SOWrapper wrap = getSharedObjectWrapper(sharedObjectID);
        if (wrap != null) {
            wrap.deliverEvent(new ChatMembershipEvent(from,add));
        }    	
    }
    protected void handleXMPPMessage(Packet aPacket) {
    	try {
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
		} catch (IOException e) {
			logException("Exception in handleXMPPMessage", e);
		}
    }
    protected void handleInvitationMessage(XMPPConnection arg0, String arg1, String arg2, String arg3, String arg4, Message arg5) {
        SOWrapper wrap = getSharedObjectWrapper(sharedObjectID);
        if (wrap != null) {
            wrap.deliverEvent(new InvitationReceivedEvent(arg0,arg1,arg2,arg3,arg4,arg5));
        }
    }
    protected void initializeSharedObject() throws IDInstantiationException {
        sharedObjectID = IDFactory.getDefault().makeStringID(XMPP_GROUP_CHAT_SHARED_OBJECT_ID);
        sharedObject = new XMPPGroupChatSharedObject(usernamespace,xmppconnection);
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
    
    protected void handleConnectionClosed(Exception e) {
    	
    }
    
	public void connect(ID remote, IConnectContext connectContext)
			throws ContainerConnectException {
		if (!(remote instanceof XMPPRoomID)) {
			throw new ContainerConnectException("remote " + remote
					+ " is not of room id type");
		}
		XMPPRoomID roomID = (XMPPRoomID) remote;
		fireContainerEvent(new SharedObjectContainerConnectingEvent(this
				.getID(), remote, connectContext));
		synchronized (getConnectLock()) {
			try {
				connectionState = CONNECTING;
				remoteServerID = null;
				addSharedObjectToContainer(remote);
				xmppconnection.addConnectionListener(new ConnectionListener() {
					public void connectionClosed() {
						handleConnectionClosed(null);
					}
					public void connectionClosedOnError(Exception arg0) {
						handleConnectionClosed(arg0);
					}
				});
				multiuserchat = new MultiUserChat(xmppconnection, roomID
						.getMucString());
				// Get nickname from join context
				String nick = null;
				try {
					Callback[] callbacks = new Callback[1];
					callbacks[0] = new NameCallback("Nickname", roomID
							.getNickname());
					if (connectContext != null) {
						CallbackHandler handler = connectContext
								.getCallbackHandler();
						if (handler != null) {
							handler.handle(callbacks);
						}
					}
					if (callbacks[0] instanceof NameCallback) {
						NameCallback cb = (NameCallback) callbacks[0];
						nick = cb.getName();
					}
				} catch (Exception e) {
					throw new ContainerConnectException(
							"Exception in CallbackHandler.handle(<callbacks>)",
							e);
				}
				String nickname = null;
				if (nick == null || nick.equals(""))
					nickname = roomID.getNickname();
				else
					nickname = nick;
				// multiuserchat.create(nickname);
				// multiuserchat.sendConfigurationForm(new
				// Form(Form.TYPE_SUBMIT));
				multiuserchat.addMessageListener(new PacketListener() {
					public void processPacket(Packet arg0) {
						handleXMPPMessage(arg0);
					}
				});
				multiuserchat.addParticipantListener(new PacketListener() {
					public void processPacket(Packet arg0) {
						handleXMPPMessage(arg0);
					}
				});
				multiuserchat
						.addParticipantStatusListener(new ParticipantStatusListener() {
							public void joined(String arg0) {
								handleChatMembershipEvent(arg0, true);
							}
							public void left(String arg0) {
								handleChatMembershipEvent(arg0, false);
							}
							public void kicked(String arg0) {
								// TODO Auto-generated method stub
								System.out.println("kicked(" + arg0 + ")");
							}
							public void voiceGranted(String arg0) {
								// TODO Auto-generated method stub
								System.out
										.println("voiceGranted(" + arg0 + ")");
							}
							public void voiceRevoked(String arg0) {
								// TODO Auto-generated method stub
								System.out
										.println("voiceRevoked(" + arg0 + ")");
							}
							public void banned(String arg0) {
								// TODO Auto-generated method stub
								System.out.println("banned(" + arg0 + ")");
							}
							public void membershipGranted(String arg0) {
								// TODO Auto-generated method stub
								System.out.println("membershipGranted(" + arg0
										+ ")");
							}
							public void membershipRevoked(String arg0) {
								// TODO Auto-generated method stub
								System.out.println("membershipRevoked(" + arg0
										+ ")");
							}
							public void moderatorGranted(String arg0) {
								// TODO Auto-generated method stub
								System.out.println("moderatorGranted(" + arg0
										+ ")");
							}
							public void moderatorRevoked(String arg0) {
								// TODO Auto-generated method stub
								System.out.println("moderatorRevoked(" + arg0
										+ ")");
							}
							public void ownershipGranted(String arg0) {
								// TODO Auto-generated method stub
								System.out.println("ownershipGranted(" + arg0
										+ ")");
							}
							public void ownershipRevoked(String arg0) {
								// TODO Auto-generated method stub
								System.out.println("ownershipRevoked(" + arg0
										+ ")");
							}
							public void adminGranted(String arg0) {
								// TODO Auto-generated method stub
								System.out
										.println("adminGranted(" + arg0 + ")");
							}
							public void adminRevoked(String arg0) {
								// TODO Auto-generated method stub
								System.out
										.println("adminRevoked(" + arg0 + ")");
							}
							public void nicknameChanged(String arg0) {
								// TODO Auto-generated method stub
								System.out.println("nicknameChanged(" + arg0
										+ ")");
							}
						});
				multiuserchat
						.addInvitationRejectionListener(new InvitationRejectionListener() {
							public void invitationDeclined(String arg0,
									String arg1) {
								// TODO Auto-generated method stub
								System.out.println("invitationDeclined(" + arg0
										+ "," + arg1 + ")");
							}
						});
				MultiUserChat.addInvitationListener(xmppconnection,
						new InvitationListener() {
							public void invitationReceived(XMPPConnection arg0,
									String arg1, String arg2, String arg3,
									String arg4, Message arg5) {
								handleInvitationMessage(arg0, arg1, arg2, arg3,
										arg4, arg5);
							}
						});
				multiuserchat.join(nickname);
				connectionState = CONNECTED;
				remoteServerID = roomID;
				fireContainerEvent(new SharedObjectContainerConnectedEvent(this
						.getID(), roomID));
			} catch (Exception e) {
				cleanUpConnectFail();
				ContainerConnectException ce = new ContainerConnectException(
						"Exception joining " + roomID);
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
            	try {
            		multiuserchat.leave();
            	} catch (Exception e) {
            		dumpStack("Exception in multi user chat.leave",e);
            	}
            }
            connectionState = UNCONNECTED;
            this.xmppconnection = null;
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
		String username = xmppconnection.getUser();
		int atIndex = username.indexOf('@');
		if (atIndex > 0) username = username.substring(0,atIndex);
		String host = xmppconnection.getHost();
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
	public IChatMessageSender getChatMessageSender() {
		return new IChatMessageSender() {
			public void sendMessage(String messageBody) throws IOException {
				if (multiuserchat != null) {
					try {
						multiuserchat.sendMessage(messageBody);
					} catch (Exception e) {
						IOException except = new IOException("Send message exception");
					    except.setStackTrace(e.getStackTrace());
					    throw except;
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
			throw new ContainerConnectException("Exception creating chat room id",e);
		}
		this.connect(targetID,null);
	}
	public void addChatParticipantListener(IChatParticipantListener participantListener) {
		if (sharedObject != null) {
			sharedObject.addChatParticipantListener(participantListener);
		}
	}
	public void addInvitationListener(IInvitationListener invitationListener) {
		if (sharedObject != null) {
			sharedObject.addInvitationListener(invitationListener);
		}
	}
}