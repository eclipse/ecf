/****************************************************************************
* Copyright (c) 2004 Composent, Inc. and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Composent, Inc. - initial API and implementation
*****************************************************************************/

package org.eclipse.ecf.provider.generic;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.Socket;
import org.eclipse.ecf.core.ISharedObjectContainerConfig;
import org.eclipse.ecf.core.ISharedObjectContainerGroupManager;
import org.eclipse.ecf.core.SharedObjectContainerJoinException;
import org.eclipse.ecf.core.comm.IAsynchConnection;
import org.eclipse.ecf.core.comm.ISynchAsynchConnection;
import org.eclipse.ecf.core.comm.ISynchConnection;
import org.eclipse.ecf.core.events.SharedObjectContainerDepartedEvent;
import org.eclipse.ecf.core.events.SharedObjectContainerEjectedEvent;
import org.eclipse.ecf.core.events.SharedObjectContainerJoinedEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.provider.generic.gmm.Member;

public class ServerSOContainer extends SOContainer implements ISharedObjectContainerGroupManager {
    public ServerSOContainer(ISharedObjectContainerConfig config) {
        super(config);
    }

    public boolean isGroupServer() {
        return true;
    }
    public Object getAdapter(Class clazz) {
        if (clazz.equals(ISharedObjectContainerGroupManager.class)) {
            debug("getAdapter()");
            return this;
        } else return null;
    }
    public boolean isGroupManager() {
        return true;
    }

    public ID getGroupID() {
        return getID();
    }

    protected void queueContainerMessage(ContainerMessage message)
            throws IOException {
        if (message.getToContainerID() == null) {
            queueToAll(message);
        } else {
            IAsynchConnection conn = getConnectionForID(message
                    .getToContainerID());
            if (conn != null)
                conn.sendAsynch(message.getToContainerID(),
                        getBytesForObject(message));
        }
    }

    protected void forwardToRemote(ID from, ID to, ContainerMessage data)
            throws IOException {
        queueContainerMessage(new ContainerMessage(from, to,
                getNextSequenceNumber(), data.getData()));
    }

    protected void forwardExcluding(ID from, ID excluding, ContainerMessage data)
            throws IOException {
        if (excluding == null) {
            queueContainerMessage(new ContainerMessage(from, null,
                    getNextSequenceNumber(), data.getData()));
        } else {
            Object ms[] = groupManager.getMembers();
            for (int i = 0; i < ms.length; i++) {
                Member m = (Member) ms[i];
                ID oldID = m.getID();
                if (!excluding.equals(oldID) && !from.equals(oldID)) {
                    IAsynchConnection conn = (IAsynchConnection) m.getData();
                    if (conn != null) {
                        try {
                            conn.sendAsynch(oldID,
                                    getBytesForObject(new ContainerMessage(
                                            from, oldID,
                                            getNextSequenceNumber(), data
                                                    .getData())));
                        } catch (IOException e) {
                            logException("Exception in forwardExcluding from "
                                    + from + " with oldID " + oldID, e);
                        }
                    }
                }
            }
        }
    }

    protected void handleViewChangeMessage(ContainerMessage mess)
            throws IOException {
        // ServerApplication should never receive change messages
    }

    public void joinGroup(ID group, Object data)
            throws SharedObjectContainerJoinException {
        SharedObjectContainerJoinException e = new SharedObjectContainerJoinException(
                "ServerApplication cannot join group " + group.getName());
        throw e;
    }

    public void leaveGroup() {
        ejectAllGroupMembers(null);
    }

    protected ContainerMessage acceptNewClient(Socket socket, String target,
            Serializable data, ISynchAsynchConnection conn) {
        try {
            ContainerMessage mess = (ContainerMessage) data;
            if (mess == null)
                throw new InvalidObjectException("container message is null");
            ID remoteID = mess.getFromContainerID();
            if (remoteID == null)
                throw new InvalidObjectException("remote id is null");
            ContainerMessage.JoinGroupMessage jgm = (ContainerMessage.JoinGroupMessage) mess
                    .getData();
            if (jgm == null)
                throw new IOException("join group message is null");
            ID memberIDs[] = null;
            synchronized (getGroupMembershipLock()) {
                if (isClosing) {
                    Exception e = new InvalidObjectException(
                            "container is closing");
                    throw e;
                }
                if (addNewRemoteMember(remoteID, conn)) {
                    // Notify existing remotes about new member
                    try {
                        forwardExcluding(getID(), remoteID, ContainerMessage
                                .makeViewChangeMessage(getID(), remoteID,
                                        getNextSequenceNumber(),
                                        new ID[] { remoteID }, true, null));
                    } catch (IOException e) {
                    }
                    // Get current membership
                    memberIDs = groupManager.getMemberIDs();
                    // Start messaging to new member
                    conn.start();
                } else {
                    ConnectException e = new ConnectException(
                            "server refused connection");
                    throw e;
                }
            }
            // notify listeners
            fireContainerEvent(new SharedObjectContainerJoinedEvent(this.getID(),remoteID));
            
            return ContainerMessage.makeViewChangeMessage(getID(), remoteID,
                    getNextSequenceNumber(), memberIDs, true, null);
        } catch (Exception e) {
            logException("Exception in acceptNewClient(" + socket + ","
                    + target + "," + data + "," + conn, e);
            // And then return null...which means refusal
            return null;
        }
    }
    protected Object checkJoin(String hostname, ID id, Serializable data)
            throws Exception {
        return null;
    }
    protected void handleLeaveGroupMessage(ContainerMessage mess) {
        ID fromID = mess.getFromContainerID();
        if (fromID == null) return;
        ID toID = mess.getToContainerID();
        debug("Member "+fromID+"leaving group");
        synchronized (getGroupMembershipLock()) {
            IAsynchConnection conn = getConnectionForID(fromID);
            if (conn == null) return;
            memberLeave(fromID,conn);
        }
        // Notify listeners
        fireContainerEvent(new SharedObjectContainerDepartedEvent(getID(),fromID));
    }

    public void ejectGroupMember(ID memberID, Serializable reason) {
        if (memberID == null) return;
        ISynchConnection conn = null;
        synchronized (getGroupMembershipLock()) {
            conn = getSynchConnectionForID(memberID);
            if (conn == null)
                return;
            try {
                conn.sendSynch(memberID, getBytesForObject(ContainerMessage
                        .makeLeaveGroupMessage(getID(), memberID,
                                getNextSequenceNumber(), reason)));
            } catch (Exception e) {
                logException("Exception in ejectGroupMember.sendAsynch()",e);
            }
            memberLeave(memberID, conn);
        }
        // Notify listeners
        fireContainerEvent(new SharedObjectContainerEjectedEvent(memberID,getID(),reason));        
    }

    public void ejectAllGroupMembers(Serializable reason) {
        synchronized (getGroupMembershipLock()) {
            Object[] members = groupManager.getMembers();
            for (int i = 0; i < members.length; i++) {
                ejectGroupMember(((Member) members[i]).getID(),reason);
            }
        }
    }

    // Support methods
    protected ID getIDForConnection(IAsynchConnection conn) {
        Object ms[] = groupManager.getMembers();
        for (int i = 0; i < ms.length; i++) {
            Member m = (Member) ms[i];
            if (conn == (IAsynchConnection) m.getData())
                return m.getID();
        }
        return null;
    }

    protected IAsynchConnection getConnectionForID(ID memberID) {
        Member mem = groupManager.getMemberForID(memberID);
        if (mem == null || !(mem.getData() instanceof IAsynchConnection))
            return null;
        return (IAsynchConnection) mem.getData();
    }

    protected ISynchConnection getSynchConnectionForID(ID memberID) {
        Member mem = groupManager.getMemberForID(memberID);
        if (mem == null || !(mem.getData() instanceof ISynchConnection))
            return null;
        
        return (ISynchConnection) mem.getData();
    }

    private final void queueToAll(ContainerMessage message) {
        Object[] members = groupManager.getMembers();
        for (int i = 0; i < members.length; i++) {
            IAsynchConnection conn = (IAsynchConnection) ((Member) members[i])
                    .getData();
            if (conn != null) {
                try {
                    conn.sendAsynch(message.getToContainerID(),
                            getBytesForObject(message));
                } catch (IOException e) {
                    logException("Exception in queueToAll for ContainerMessage "+message,e);
                }
            }
        }
    }

    public void dispose(long timeout) {
        // For servers, we'll eject all members
        ejectAllGroupMembers(null);
        super.dispose(timeout);
    }
}