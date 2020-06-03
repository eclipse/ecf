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
	 * @param targetID
	 *            the {@link ID} of the target user for the presence update. If
	 *            <code>null</code>, the presence update is sent to all users
	 *            in current roster.  If non-<code>null</code> the presence update
	 *            is sent only to the given <code>targetID</code>.
	 * @param presence
	 *            the presence information. Should not be <code>null</code>.
	 * 
	 * @exception ECFException
	 *                thrown if request cannot be sent (e.g. because of previous
	 *                disconnect).
	 */
	public void sendPresenceUpdate(ID targetID, IPresence presence)
			throws ECFException;

}
