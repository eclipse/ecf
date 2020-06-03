/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.presence.bot;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.presence.chatroom.IChatRoomContainer;

/**
 * Advisor instance for receiving pre connect events for chat rooms.
 */
public interface IChatRoomContainerAdvisor extends IContainerAdvisor {

	/**
	 * This method will be called prior to connecting to the
	 * <code>roomContainer</code>. The given <code>roomContainer</code> and
	 * <code>roomID</code> will not be <code>null</code>.
	 * 
	 * @param roomContainer
	 *            the {@link IChatRoomContainer} that will be connected to. Will
	 *            not be <code>null</code>.
	 * 
	 * @param roomID
	 *            the {@link ID} of the room that will be connected to. Will not
	 *            be <code>null</code>.
	 */
	public void preChatRoomConnect(IChatRoomContainer roomContainer, ID roomID);

}
