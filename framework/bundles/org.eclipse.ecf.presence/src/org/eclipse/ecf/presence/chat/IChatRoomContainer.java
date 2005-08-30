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
package org.eclipse.ecf.presence.chat;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.presence.IInvitationListener;
import org.eclipse.ecf.presence.IMessageListener;
import org.eclipse.ecf.presence.IMessageSender;
import org.eclipse.ecf.presence.IParticipantListener;

/**
 * Chat container
 */
public interface IChatRoomContainer extends IContainer {
	
    /**
     * Setup listener for handling IM messages
     * @param listener
     */
	public void addMessageListener(IMessageListener msgListener);

	/**
	 * Get interface for sending messages
	 * @return IMessageSender.  Null if no message sender available
	 */
    public IMessageSender getMessageSender();

    /**
     * Add invitation listener.  The given listener will be notified if/when
     * invitiations are received or invitation rejections are received
     * @param invitationListener
     */
    public void addInvitationListener(IInvitationListener invitationListener);
    
    /**
     * Add participant listener.  The given listener will be notified if/when
     * participants are added or removed from given room
     * @param participantListener
     */
    public void addParticipantListener(IParticipantListener participantListener);
}
