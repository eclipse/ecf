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
    Serializable data;

    public RemoteSharedObjectEvent(ID sender, ID cont, Serializable data) {
        super();
        this.senderSharedObjectID = sender;
        this.containerID = cont;
    }

    public ID getContainer() {
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
    public Event getEvent() {
        return this;
    }
    public Serializable getData() {
        return data;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("RemoteSharedObjectEvent[sender:");
        if (senderSharedObjectID != null)
            sb.append(senderSharedObjectID.getName()).append(",");
        else
            sb.append("null,");
        sb.append("container:");
        if (containerID != null)
            sb.append(containerID.getName()).append(",");
        sb.append("data:");
        sb.append(data).append("]");
        return sb.toString();
    }
}