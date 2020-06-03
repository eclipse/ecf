/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
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

package org.eclipse.ecf.presence.chatroom;

import org.eclipse.ecf.core.util.ECFException;

/**
 * Perform administrative functions for an IChatRoomContainer.
 * 
 * @since 1.1
 */
public interface IChatRoomAdminSender {

	/**
	 * Send chat room subject change.
	 * 
	 * @param newsubject the new subject for the chat room.
	 * @throws ECFException exception thrown if some problem sending message (e.g. disconnect).
	 */
	public void sendSubjectChange(String newsubject) throws ECFException;
}
