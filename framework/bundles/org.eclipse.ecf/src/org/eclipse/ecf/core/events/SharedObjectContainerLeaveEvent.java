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

public class SharedObjectContainerLeaveEvent implements ContainerEvent {

    ID containerID = null;
    ID groupManagerID = null;

    Serializable data = null;

    public SharedObjectContainerLeaveEvent(ID containerID, ID groupManagerID) {
        this.containerID = containerID;
        this.groupManagerID = groupManagerID;
    }
    public ID getContainerID() {
        return containerID;
    }

    public ID getGroupManager() {
        return groupManagerID;
    }
    public void setData(Serializable data) {
        this.data = data;
    }

}