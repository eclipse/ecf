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

public class SharedObjectContainerJoinedEvent implements
        ISharedObjectContainerJoinedEvent {
	private static final long serialVersionUID = 3833467322827617078L;
	private final ID joinedContainerID;
    private final ID localContainerID;

    public SharedObjectContainerJoinedEvent(ID local, ID joinContainerID) {
        super();
        this.localContainerID = local;
        this.joinedContainerID = joinContainerID;
    }

    public ID getJoinedContainerID() {
        return joinedContainerID;
    }

    public ID getLocalContainerID() {
        return localContainerID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buf = new StringBuffer(
                "SharedObjectContainerJoinedEvent[");
        buf.append(getJoinedContainerID())
                .append(";");
        buf.append(getLocalContainerID()).append("]");
        return buf.toString();
    }
}