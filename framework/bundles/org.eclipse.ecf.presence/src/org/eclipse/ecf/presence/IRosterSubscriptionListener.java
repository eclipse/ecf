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
 * 
 * @deprecated See replacement interface and implementation in
 *             <code>org.eclipse.ecf.presence.roster</code> package
 * 
 */
public interface IRosterSubscriptionListener {

	/**
	 * Receive subscribe request.
	 * 
	 * @param fromID
	 *            the sender of the subscribe request
	 */
	public void handleSubscribeRequest(ID fromID);

	/**
	 * Receive subscribed notification.
	 * 
	 * @param fromID
	 *            the sender of the subscribed notification
	 */
	public void handleSubscribed(ID fromID);

	/**
	 * Receive unsubscribed notification.
	 * 
	 * @param fromID
	 *            the sender of the unsubscribed notification
	 */
	public void handleUnsubscribed(ID fromID);
}
