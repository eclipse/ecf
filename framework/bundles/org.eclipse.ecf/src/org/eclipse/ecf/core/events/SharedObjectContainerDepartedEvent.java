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

public class SharedObjectContainerDepartedEvent implements ContainerEvent {

    ID other;
    ID containerID;

    public SharedObjectContainerDepartedEvent(ID container, ID o) {
        super();
        this.containerID = container;
        this.other = o;
    }

    public ID getDepartedContainerID() {
        return other;
    }
    public ID getLocalContainerID() {
        return containerID;
    }
}