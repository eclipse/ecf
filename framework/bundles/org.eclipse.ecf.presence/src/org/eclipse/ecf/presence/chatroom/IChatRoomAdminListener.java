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

package org.eclipse.ecf.presence.chatroom;

import org.eclipse.ecf.core.identity.ID;

/**
 * Listener for chat room subject changes
 */
public interface IChatRoomAdminListener {

	/**
	 * Handle notification of new subject set for the associated chat room.
	 * 
	 * @param from
	 *            the ID of the user the subject change is from. May be
	 *            <code>null</code> if user is not known, or change is not
	 *            from any particular user (i.e. the system).
	 * 
	 * @param newSubject
	 *            the new subject for the chat room. Will not be
	 *            <code>null</code>, but may be empty String.
	 */
	public void handleSubjectChange(ID from, String newSubject);
}
