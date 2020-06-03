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

import org.eclipse.ecf.datashare.events.IChannelContainerEvent;

/**
 * Listener for channel container events. The following types of events can be
 * received via this listener:
 * <p>
 * IChannelContainerChannelActivatedEvent - delivered when a channel within this
 * container is activated
 * <p>
 * IChannelContainerChannelDeactivatedEvent - delivered when a channel within
 * this container is deactivated
 * 
 */
public interface IChannelContainerListener {
	/**
	 * Handle channel container events.
	 * 
	 * @param event
	 *            IChannelContainerAdapter event. Will not be <code>null</code>.
	 */
	public void handleChannelContainerEvent(IChannelContainerEvent event);
}
