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
import org.eclipse.ecf.core.util.Event;

public class LocalSharedObjectEvent implements SharedObjectEvent {

    ID sender;
    Event event;

    public LocalSharedObjectEvent(ID s, Event evt) {
        super();
        this.sender = s;
        this.event = evt;
    }

    public ID getSenderSharedObjectID() {
        return sender;
    }
    public Event getEvent() {
        return event;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("LocalSharedObjectEvent[sender:");
        if (sender != null)
            sb.append(sender.getName()).append(",");
        else
            sb.append("null,");
        sb.append("event:").append(event).append("]");
        return sb.toString();
    }
}