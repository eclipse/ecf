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

public class SharedObjectActivatedEvent implements ContainerEvent {

    ID activated;
    ID[] members;
    ID containerID;

    public SharedObjectActivatedEvent(ID container, ID act, ID[] others) {
        super();
        this.containerID = container;
        this.activated = act;
        this.members = others;
    }

    public ID getActivatedID() {
        return activated;
    }

    public ID getLocalContainerID() {
        return containerID;
    }
    public ID[] getGroupMemberIDs() {
        return members;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(
                "SharedObjectActivatedEvent[activated:");
        if (activated != null)
            sb.append(activated.getName()).append(",");
        else
            sb.append("null,");
        sb.append("[");
        for (int i = 0; i < members.length; i++) {
            sb.append(members[i].getName());
            if (i < members.length - 1)
                sb.append(",");
        }
        sb.append("]]");
        return sb.toString();
    }
}