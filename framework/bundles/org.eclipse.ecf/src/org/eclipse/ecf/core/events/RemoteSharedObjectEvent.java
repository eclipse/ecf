/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.core.events;

import java.io.Serializable;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.Event;

public class RemoteSharedObjectEvent implements SharedObjectEvent, Serializable {

    ID senderSharedObjectID;
    ID containerID;
    ID remoteContainerID;
    Object data;

    public RemoteSharedObjectEvent(ID senderObj, ID localCont, ID remoteCont, Object data) {
        super();
        this.senderSharedObjectID = senderObj;
        this.containerID = localCont;
        this.remoteContainerID = remoteCont;
        this.data = data;
    }

    public ID getLocalContainerID() {
        return containerID;
    }
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.api.events.SharedObjectEvent#getSenderSharedObject()
     */
    public ID getSenderSharedObjectID() {
        return senderSharedObjectID;
    }
    public ID getRemoteContainerID() {
        return remoteContainerID;
    }
    public Event getEvent() {
        return this;
    }
    public Object getData() {
        return data;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("RemoteSharedObjectEvent[sender:");
        sb.append("soid:").append((senderSharedObjectID==null)?"null":senderSharedObjectID.getName()).append(";");
        sb.append("lcont:").append((containerID==null)?"null":containerID.getName()).append(";");
        sb.append("rcont:").append((remoteContainerID==null)?"null":remoteContainerID.getName()).append(";");
        sb.append("data:").append("]");
        return sb.toString();
    }
}