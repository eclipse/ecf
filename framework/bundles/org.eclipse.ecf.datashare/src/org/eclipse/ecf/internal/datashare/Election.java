/*******************************************************************************
 * Copyright (c) 2005 Peter Nehrer and Composent, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Peter Nehrer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.internal.datashare;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.ecf.core.identity.ID;

/**
 * @author pnehrer
 */
public class Election {

    public static final short LOST = 0;

    public static final short WON = 1;

    public static final short UNKNOWN = 2;

    private final long ticket;

    private final HashSet members;

    public Election(long ticket, Collection members) {
        this.ticket = ticket;
        this.members = new HashSet(members);
    }

    public synchronized short processVote(long ticket, ID containerID) {
        if (this.ticket < ticket)
            return LOST;
        else {
            members.remove(containerID);
            return members.isEmpty() ? WON : UNKNOWN;
        }
    }

    public synchronized short disqualify(ID containerID) {
        members.remove(containerID);
        return members.isEmpty() ? WON : UNKNOWN;
    }
}