/****************************************************************************
 * Copyright (c) 2004 Composent, Inc., Peter Nehrer, Boris Bokowski.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.datashare.events;

import org.eclipse.ecf.core.identity.ID;

/**
 * Channel message event. This event is received by IChannelListeners when a
 * remote sends a message (via IChannel.sendMessage)
 * 
 */
public interface IChannelMessageEvent extends IChannelEvent {
	/**
	 * Get ID of sender container
	 * 
	 * @return ID of sender's container. Will not be <code>null</code>.
	 */
	public ID getFromContainerID();

	/**
	 * Get data associated with message. This method returns the data actually
	 * included in the IChannel.sendMessage(<data>).
	 * 
	 * @return byte [] data associated with channel message. Will not be
	 *         <code>null</code>, but may be empty array byte[0].
	 */
	public byte[] getData();
}
