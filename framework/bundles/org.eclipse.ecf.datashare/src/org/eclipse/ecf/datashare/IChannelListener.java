/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc., Peter Nehrer, Boris Bokowski. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.datashare;

import org.eclipse.ecf.datashare.events.IChannelEvent;

/**
 * Listener for receiving messages sent to a given channel. The following types
 * of events can be received via this listener:
 * <p>
 * IChannelMessageEvent - delivered when channel receives a message
 * <p>
 * IChannelConnectEvent - delivered when container channel is in joins a group
 * <p>
 * IChannelDisconnectEvent - delivered when container channel is in departs
 * 
 */
public interface IChannelListener {
	/**
	 * Handle events sent to the channel.
	 * 
	 * @param event
	 *            the event received. Will not be <code>null</code>.
	 */
	public void handleChannelEvent(IChannelEvent event);
}
