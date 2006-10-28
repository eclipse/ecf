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
 * Listener for handling notifications of subscribe/unsubscribe requests.
 * Implementers of this interface must be registered via
 * {@link IPresenceContainerAdapter#addSubscribeListener(ISubscribeListener)}
 * 
 * @see IPresenceContainerAdapter
 * 
 */
public interface ISubscribeListener {

	/**
	 * Receive subscribe request.
	 * 
	 * @param fromID
	 *            the sender of the subscribe request
	 * @param presence
	 *            the presence information associated with the user making the
	 *            request
	 */
	public void handleSubscribeRequest(ID fromID, IPresence presence);

	/**
	 * Receive unsubscribe request.
	 * 
	 * @param fromID
	 *            the sender of the unsubscribe request
	 * @param presence
	 *            the presence information associated with the user making the
	 *            request
	 */
	public void handleUnsubscribeRequest(ID fromID, IPresence presence);

	/**
	 * Receive subscribed notification.
	 * 
	 * @param fromID
	 *            the sender of the subscribed notification
	 * @param presence
	 *            the presence information associated with the user sending the
	 *            notification
	 */
	public void handleSubscribed(ID fromID, IPresence presence);

	/**
	 * Receive unsubscribed notification.
	 * 
	 * @param fromID
	 *            the sender of the unsubscribed notification
	 * @param presence
	 *            the presence information associated with the user sending the
	 *            notification
	 */
	public void handleUnsubscribed(ID fromID, IPresence presence);
}
