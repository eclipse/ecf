/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.events;

import org.eclipse.ecf.core.identity.ID;

public class SharedObjectContainerLeaveGroupEvent implements
        ISharedObjectContainerLeaveGroupEvent {
	private static final long serialVersionUID = 3257570607204742200L;
	ID localContainerID;
    ID groupID;

    public SharedObjectContainerLeaveGroupEvent(ID localContainerID, ID groupID) {
        this.localContainerID = localContainerID;
        this.groupID = groupID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.events.IContainerEvent#getLocalContainerID()
     */
    public ID getLocalContainerID() {
        return localContainerID;
    }

    public ID getGroupID() {
        return groupID;
    }
    public String toString() {
        StringBuffer buf = new StringBuffer("SharedObjectContainerLeaveGroupEvent[");
        buf.append(getLocalContainerID()).append(";");
        buf.append(getGroupID()).append("]");
        return buf.toString();
    }
}