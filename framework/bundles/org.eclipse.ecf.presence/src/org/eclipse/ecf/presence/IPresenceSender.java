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
import org.eclipse.ecf.core.util.ECFException;

/**
 * Send presence change events to remotes on buddy list.
 * 
 */
public interface IPresenceSender {

	/**
	 * Send a presence update to a remote user
	 * 
	 * @param toID
	 *            the target user. Should not be null.
	 * @param presence
	 *            the presence information. Should not be null.
	 * 
	 * @exception ECFException
	 *                thrown if request cannot be sent (e.g. because of previous
	 *                disconnect
	 */
	public void sendPresenceUpdate(ID toID, IPresence presence)
			throws ECFException;

}
