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
package org.eclipse.ecf.presence.roster;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;

public interface IRosterSubscriptionSender {

	/**
	 * Send a roster add request (subscribe) to a remote
	 * @param user
	 *            the account name of the target user. Should not be null.
	 * @param groups
	 *            an array of group names that this use will belong to on the
	 *            roster entry. Should not be null.
	 * 
	 * @exception ECFException
	 *                thrown if request cannot be sent (e.g. because of previous
	 *                disconnect
	 */
	public void sendRosterAdd(String user, String name, String[] groups) throws ECFException;

	/**
	 * Send roster remove request (unsubscribe) to a remote
	 * @param userID
	 *            the user id the request it intended for. Should not be null
	 * 
	 * @exception ECFException
	 *                thrown if request cannot be sent (e.g. because of previous
	 *                disconnect
	 */
	public void sendRosterRemove(ID userID) throws ECFException;

}
