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

public class SharedObjectDeactivatedEvent implements ContainerEvent {

    ID deactivated;
    ID containerID;

    public SharedObjectDeactivatedEvent(ID container, ID deact) {
        super();
        this.containerID = container;
        this.deactivated = deact;
    }
    public ID getDeactivated() {
        return deactivated;
    }
    public ID getContainerID() {
        return containerID;
    }
    public String toString() {
        StringBuffer sb = new StringBuffer(
                "SharedObjectDeactivatedEvent[deactivated:");
        if (deactivated != null)
            sb.append(deactivated.getName());
        else
            sb.append("null");
        sb.append("]");
        return sb.toString();
    }
}