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

public class SharedObjectContainerEjectedEvent implements ContainerEvent {

    ID containerID = null;
    ID groupManagerID = null;

    Serializable data = null;

    public SharedObjectContainerEjectedEvent(ID containerID, ID groupManagerID,
            Serializable data) {
        this.containerID = containerID;
        this.groupManagerID = groupManagerID;
        this.data = data;
    }
    public ID getContainerID() {
        return containerID;
    }

    public ID getGroupManager() {
        return groupManagerID;
    }
    public Serializable getData() {
        return data;
    }

}