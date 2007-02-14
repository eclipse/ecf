/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.generic;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;

import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.events.ContainerConnectedEvent;
import org.eclipse.ecf.core.events.ContainerConnectingEvent;
import org.eclipse.ecf.core.events.ContainerDisconnectedEvent;
import org.eclipse.ecf.core.events.ContainerDisconnectingEvent;
import org.eclipse.ecf.core.events.ContainerEjectedEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.security.Callback;
import org.eclipse.ecf.core.security.CallbackHandler;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.security.IConnectInitiatorPolicy;
import org.eclipse.ecf.core.security.UnsupportedCallbackException;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainerClient;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainerConfig;
import org.eclipse.ecf.core.sharedobject.SharedObjectDescription;
import org.eclipse.ecf.internal.provider.Messages;
import org.eclipse.ecf.provider.comm.AsynchEvent;
import org.eclipse.ecf.provider.comm.ConnectionCreateException;
import org.eclipse.ecf.provider.comm.DisconnectEvent;
import org.eclipse.ecf.provider.comm.IAsynchConnection;
import org.eclipse.ecf.provider.comm.IConnection;
import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
import org.eclipse.ecf.provider.comm.SynchEvent;
import org.eclipse.ecf.provider.generic.gmm.Member;

public abstract class ClientSOContainer extends SOContainer implements
		ISharedObjectContainerClient {
	
	public static final int DEFAULT_CONNECT_TIMEOUT = 30000;
	
	protected ISynchAsynchConnection connection;

	protected ID remoteServerID;

	protected byte connectionState;

	protected IConnectInitiatorPolicy connectPolicy = null;
	
	public static final byte DISCONNECTED = 0;

	public static final byte CONNECTING = 1;

	public static final byte CONNECTED = 2;

	static final class Lock {
	}

	protected Lock connectLock;

	protected Lock getConnectLock() {
		return connectLock;
	}

	protected ISynchAsynchConnection getConnection() {
		return connection;
	}

	public ClientSOContainer(ISharedObjectContainerConfig config) {
		super(config);
		connection = null;
		connectionState = DISCONNECTED;
		connectLock = new Lock();
	}

	public void setConnectInitiatorPolicy(IConnectInitiatorPolicy policy) {
		this.connectPolicy = policy;
	}
	
	public void dispose() {
		synchronized (connectLock) {
			isClosing = true;
			if (isConnected()) {
				this.disconnect();
			} else if (isConnecting()) {
				killConnection(connection);
			}
			remoteServerID = null;
		}
		super.dispose();
	}

	public final boolean isGroupManager() {
		return false;
	}

	public ID getConnectedID() {
		synchronized (getConnectLock()) {
			return remoteServerID;
		}
	}

	private void setStateDisconnected(ISynchAsynchConnection conn) {
		killConnection(conn);
		connectionState = DISCONNECTED;
		connection = null;
		remoteServerID = null;
	}

	private void setStateConnecting(ISynchAsynchConnection conn) {
		connectionState = CONNECTING;
		connection = conn;
	}

	private void setStateConnected(ID serverID, ISynchAsynchConnection conn) {
		connectionState = CONNECTED;
		connection = conn;
		remoteServerID = serverID;
	}

	public void connect(ID remote, IConnectContext joinContext)
			throws ContainerConnectException {
		try {
			if (isClosing)
				throw new IllegalStateException(Messages.ClientSOContainer_Container_Closing);
			debug("connect(" + remote + "," + joinContext + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			Object response = null;
			synchronized (getConnectLock()) {
				// Throw if already connected
				if (isConnected())
					throw new IllegalStateException(Messages.ClientSOContainer_Already_Connected
							+ getConnectedID());
				// Throw if connecting
				if (isConnecting())
					throw new IllegalStateException(Messages.ClientSOContainer_Currently_Connecting);
				// else we're entering connecting state
				// first notify synchonously
				fireContainerEvent(new ContainerConnectingEvent(this.getID(),
						remote, joinContext));
				ISynchAsynchConnection aConnection = createConnection(remote,
						joinContext);
				setStateConnecting(aConnection);
				synchronized (aConnection) {

					Object connectData = getConnectData(remote,joinContext);
					int connectTimeout = getConnectTimeout();
					
					try {
						// Make connect call
						response = aConnection.connect(remote, connectData,
								connectTimeout);
					} catch (IOException e) {
						if (getConnection() != aConnection)
							killConnection(aConnection);
						else
							setStateDisconnected(aConnection);
						throw e;
					}
					// If not in correct state, disconnect and return
					if (getConnection() != aConnection) {
						killConnection(aConnection);
						throw new IllegalStateException(
								Messages.ClientSOContainer_Connect_Failed_Incorrect_State);
					}
					ID serverID = null;
					try {
						serverID = handleConnectResponse(remote, response);
					} catch (Exception e) {
						setStateDisconnected(aConnection);
						throw e;
					}
					aConnection.start();
					setStateConnected(serverID, aConnection);
				}
			}
		} catch (Exception e) {
			traceStack("Exception in connect", e); //$NON-NLS-1$
			ContainerConnectException except = new ContainerConnectException(
					Messages.ClientSOContainer_Exception_Connecting + remote.getName(), e);
			throw except;
		}
	}

	protected Callback[] createAuthorizationCallbacks() {
		return null;
	}

	protected Object getConnectData(ID remote, IConnectContext joinContext) throws IOException, UnsupportedCallbackException {
		Object connectData = null;
		if (connectPolicy != null) connectData = connectPolicy.createConnectData(this, remote, joinContext);
		else {
			Callback[] callbacks = createAuthorizationCallbacks();
			if (joinContext != null) {
				CallbackHandler handler = joinContext
						.getCallbackHandler();
				if (handler != null) {
					handler.handle(callbacks);
				}
			}
		}
		return ContainerMessage.createJoinGroupMessage(getID(), remote,
				getNextSequenceNumber(), (Serializable) connectData);
	}

	protected int getConnectTimeout() {
		if (connectPolicy != null) return connectPolicy.getConnectTimeout();
		else return DEFAULT_CONNECT_TIMEOUT;
	}

	protected void handleLeaveGroupMessage(ContainerMessage mess) {
		if (!isConnected())
			return;
		ContainerMessage.LeaveGroupMessage lgm = (ContainerMessage.LeaveGroupMessage) mess
				.getData();
		ID fromID = mess.getFromContainerID();
		if (fromID == null || !fromID.equals(remoteServerID)) {
			// we ignore anything not from our server
			return;
		}
		debug("We've been ejected from group " + remoteServerID); //$NON-NLS-1$
		synchronized (getGroupMembershipLock()) {
			memberLeave(fromID, connection);
		}
		// Now notify that we've been ejected
		fireContainerEvent(new ContainerEjectedEvent(getID(), fromID, lgm
				.getData()));
	}

	protected void handleViewChangeMessage(ContainerMessage mess)
			throws IOException {
		if (!isConnected())
			return;
		debug("handleViewChangeMessage(" + mess + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		ContainerMessage.ViewChangeMessage vc = (ContainerMessage.ViewChangeMessage) mess
				.getData();
		if (vc == null)
			throw new IOException(Messages.ClientSOContainer_View_Change_Is_Null);
		ID fromID = mess.getFromContainerID();
		if (fromID == null || !fromID.equals(remoteServerID)) {
			throw new IOException(Messages.ClientSOContainer_View_Change_Message + fromID
					+ Messages.ClientSOContainer_Is_Not_Same + remoteServerID);
		}
		ID[] changeIDs = vc.getChangeIDs();
		if (changeIDs == null) {
			// do nothing if we've got no changes
		} else {
			for (int i = 0; i < changeIDs.length; i++) {
				if (vc.isAdd()) {
					boolean wasAdded = false;
					synchronized (getGroupMembershipLock()) {
						// check to make sure this member id is not already
						// known
						if (groupManager.getMemberForID(changeIDs[i]) == null) {
							wasAdded = true;
							groupManager.addMember(new Member(changeIDs[i]));
						}
					}
					// Notify listeners only if the add was actually
					// accomplished
					if (wasAdded)
						fireContainerEvent(new ContainerConnectedEvent(getID(),
								changeIDs[i]));
				} else {
					if (changeIDs[i].equals(getID())) {
						// We've been ejected.
						ID serverID = remoteServerID;
						synchronized (getGroupMembershipLock()) {
							memberLeave(remoteServerID, connection);
						}
						// Notify listeners that we've been ejected
						fireContainerEvent(new ContainerEjectedEvent(getID(),
								serverID, vc.getData()));
					} else {
						synchronized (getGroupMembershipLock()) {
							groupManager.removeMember(changeIDs[i]);
						}
						// Notify listeners that another remote has gone away
						fireContainerEvent(new ContainerDisconnectedEvent(
								getID(), changeIDs[i]));
					}
				}
			}
		}
	}

	protected void forwardExcluding(ID from, ID excluding, ContainerMessage data)
			throws IOException {
		// NOP
	}

	protected Serializable getLeaveData(ID target) {
		return null;
	}

	public void disconnect() {
		synchronized (getConnectLock()) {
			// If we are currently connected then get connection lock and send
			// disconnect message
			if (isConnected()) {
				ID groupID = getConnectedID();
				debug("disconnect(" + groupID + ")"); //$NON-NLS-1$ //$NON-NLS-2$
				fireContainerEvent(new ContainerDisconnectingEvent(
						this.getID(), groupID));
				synchronized (connection) {
					try {
						connection.sendSynch(groupID,
								serializeObject(ContainerMessage
										.createLeaveGroupMessage(getID(),
												groupID,
												getNextSequenceNumber(),
												getLeaveData(groupID))));
					} catch (Exception e) {
					}
					synchronized (getGroupMembershipLock()) {
						memberLeave(groupID, connection);
					}
				}
				// notify listeners
				fireContainerEvent(new ContainerDisconnectedEvent(this.getID(),
						groupID));
			}
		}
	}

	protected abstract ISynchAsynchConnection createConnection(ID remoteSpace,
			Object data) throws ConnectionCreateException;

	protected void queueContainerMessage(ContainerMessage message)
			throws IOException {
		// Do it
		connection.sendAsynch(message.getToContainerID(),
				serializeObject(message));
	}

	protected void forwardExcluding(ID from, ID excluding, byte msg,
			Serializable data) throws IOException { /* NOP */
	}

	protected void forwardToRemote(ID from, ID to, ContainerMessage message)
			throws IOException { /* NOP */
	}

	protected ID getIDForConnection(IAsynchConnection conn) {
		return remoteServerID;
	}

	protected void memberLeave(ID fromID, IAsynchConnection conn) {
		if (fromID.equals(remoteServerID)) {
			groupManager.removeNonLocalMembers();
			super.memberLeave(fromID, conn);
			setStateDisconnected(null);
		} else if (fromID.equals(getID())) {
			super.memberLeave(fromID, conn);
		}
	}

	protected void sendMessage(ContainerMessage data) throws IOException {
		// Get connect lock, then call super version
		synchronized (connectLock) {
			checkConnected();
			super.sendMessage(data);
		}
	}

	protected ID[] sendCreateMsg(ID toID, SharedObjectDescription createInfo)
			throws IOException {
		// Get connect lock, then call super version
		synchronized (connectLock) {
			checkConnected();
			return super.sendCreateSharedObjectMessage(toID, createInfo);
		}
	}

	protected void processDisconnect(DisconnectEvent evt) {
		// Get connect lock, and just return if this connection has been
		// terminated
		synchronized (connectLock) {
			super.processDisconnect(evt);
		}
	}

	protected void processAsynch(AsynchEvent evt) throws IOException {
		// Get connect lock, then call super version
		synchronized (connectLock) {
			checkConnected();
			super.processAsynch(evt);
		}
	}

	protected Serializable processSynch(SynchEvent evt) throws IOException {
		synchronized (connectLock) {
			checkConnected();
			IConnection conn = evt.getConnection();
			if (connection != conn)
				throw new ConnectException(Messages.ClientSOContainer_Not_Connected);
			return super.processSynch(evt);
		}
	}

	protected boolean isConnected() {
		return (connectionState == CONNECTED);
	}

	protected boolean isConnecting() {
		return (connectionState == CONNECTING);
	}

	private void checkConnected() throws ConnectException {
		if (!isConnected())
			throw new ConnectException(Messages.ClientSOContainer_Not_Connected);
	}

	protected ID handleConnectResponse(ID orginalTarget, Object serverData)
			throws Exception {
		ContainerMessage aPacket = (ContainerMessage) serverData;
		ID fromID = aPacket.getFromContainerID();
		if (fromID == null)
			throw new NullPointerException(Messages.ClientSOContainer_ServerID_Cannot_Be_Null);
		ContainerMessage.ViewChangeMessage viewChangeMessage = (ContainerMessage.ViewChangeMessage) aPacket.getData();
		// If it's not an add message then we've been refused.  Get exception info from viewChangeMessage and
		// throw if there
		if (!viewChangeMessage.isAdd()) {
			// We were refused by server...so we retrieve data and throw
			Object data = viewChangeMessage.getData();
			if (data != null && data instanceof Exception) throw (Exception) data;
			else throw new NullPointerException(Messages.ClientSOContainer_Invalid_Server_Response);
		}
		// Otherwize everything is OK to this point and we get the group member IDs from server
		ID[] ids = viewChangeMessage.getChangeIDs();
		if (ids == null)
			throw new NullPointerException(Messages.ClientSOContainer_Exception_ID_Array_Null);
		for (int i = 0; i < ids.length; i++) {
			ID id = ids[i];
			if (id != null && !id.equals(getID())) {
				addNewRemoteMember(id, null);
				// notify listeners
				fireContainerEvent(new ContainerConnectedEvent(this.getID(), id));
			}
		}
		return fromID;
	}
}