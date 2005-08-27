/****************************************************************************
* Copyright (c) 2004 Composent, Inc. and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Composent, Inc. - initial API and implementation
*****************************************************************************/
package org.eclipse.ecf.presence;

import org.eclipse.ecf.core.identity.ID;

/**
 * Listener for receiving and processing presence update events
 * 
 */
public interface IPresenceListener extends IParticipantListener {
    
	/**
	 * Notification that a new group member (i.e. the server) has
	 * successfully joined/connected
	 * @param joinedContainer
	 */
    public void handleContainerJoined(ID joinedContainer);
    /**
     *  Notification that a roster entry has been received
     * @param entry
     */
	public void handleRosterEntry(IRosterEntry entry);
	/**
	 * Notification that a roster entry has been set
	 * @param entry
	 */
	public void handleSetRosterEntry(IRosterEntry entry);
	/**
	 * Notification that a remote container (i.e. the server) has
	 * disconnected/left group
	 * 
	 * @param departedContainer
	 */
    public void handleContainerDeparted(ID departedContainer);
    
}
