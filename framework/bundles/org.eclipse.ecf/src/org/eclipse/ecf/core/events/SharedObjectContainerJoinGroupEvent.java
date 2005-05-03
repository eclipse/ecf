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

public class SharedObjectContainerJoinGroupEvent implements
        ISharedObjectContainerJoinGroupEvent {
	private static final long serialVersionUID = 3544952173248263729L;
	ID localContainerID;
    ID groupID;
    Object data;

    public SharedObjectContainerJoinGroupEvent(ID localContainerID, ID groupID,
            Object data) {
        this.localContainerID = localContainerID;
        this.groupID = groupID;
        this.data = data;
    }

    public ID getGroupID() {
        return groupID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.events.ISharedObjectContainerJoinGroupEvent#getData()
     */
    public Object getData() {
        return data;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.events.IContainerEvent#getLocalContainerID()
     */
    public ID getLocalContainerID() {
        return localContainerID;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer("SharedObjectContainerJoinGroupEvent[");
        buf.append(getLocalContainerID()).append(";");
        buf.append(getGroupID()).append(";");
        buf.append(getData()).append("]");
        return buf.toString();
    }
}