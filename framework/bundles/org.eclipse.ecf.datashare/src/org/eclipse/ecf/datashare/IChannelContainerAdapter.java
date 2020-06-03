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
package org.eclipse.ecf.datashare;

import java.util.Map;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;

/**
 * Channel container entry point adapter. This interface is an adapter to allow
 * providers to expose channels to clients. It may be used in the following way:
 * <p>
 * 
 * <pre>
 *   IChannelContainerAdapter channelcontainer = (IChannelContainerAdapter) container.getAdapter(IChannelContainerAdapter.class);
 *   if (channelcontainer != null) {
 *      // use channelcontainer
 *      ...
 *   } else {
 *      // container does not support channel container functionality
 *   }
 * </pre>
 * 
 */
public interface IChannelContainerAdapter extends
		IAbstractChannelContainerAdapter {
	/**
	 * Create a new channel within this container
	 * 
	 * @param channelID
	 *            the ID of the new channel. Must not be <code>null</code>.
	 * @param listener
	 *            a listener for receiving messages from remotes for this
	 *            channel. May be <code>null</code> if no listener is to be
	 *            notified.
	 * @param properties
	 *            a Map of properties to provide to the channel. May be
	 *            <code>null</code>.
	 * @return IChannel the new IChannel instance
	 * @throws ECFException
	 *             if some problem creating IChannel instance
	 */
	public IChannel createChannel(ID channelID, IChannelListener listener,
			Map properties) throws ECFException;

	/**
	 * Create a new channel within this container
	 * 
	 * @param newChannelConfig
	 *            the configuration for the newly created channel. Must not be
	 *            <code>null</code>.
	 * @return IChannel the new IChannel instance. Will not be <code>null</code>.
	 * @throws ECFException
	 *             if some problem creating IChannel instance
	 */
	public IChannel createChannel(IChannelConfig newChannelConfig)
			throws ECFException;
}
