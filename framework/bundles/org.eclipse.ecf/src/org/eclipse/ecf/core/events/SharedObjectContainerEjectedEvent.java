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

public class SharedObjectContainerEjectedEvent implements
        ISharedObjectContainerEjectedEvent {
    private final ID localContainerID;
    private final ID groupID;
    private final Serializable reason;
    
    public SharedObjectContainerEjectedEvent(ID containerID, ID groupID, Serializable reason) {
        super();
        this.localContainerID = containerID;
        this.groupID = groupID;
        this.reason = reason;
    }

    public ID getGroupID() {
        return groupID;
    }
    public ID getLocalContainerID() {
        return localContainerID;
    }
    public Serializable getReason() {
        return reason;
    }
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buf = new StringBuffer(
                "SharedObjectContainerEjectedEvent[");
        buf.append(getLocalContainerID()).append(";");
        buf.append(getGroupID()).append(";");
        buf.append(getReason()).append("]");
        return buf.toString();
    }
}