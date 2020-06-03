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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ecf.core.identity.IIdentifiable;

/**
 * Channel configuration to be used during createChannel to configure the newly
 * created IChannel implementation
 * 
 */
public interface IChannelConfig extends IAdaptable, IIdentifiable {
	/**
	 * Get listener for channel being created. Typically, provider will call
	 * this method during the implementation of createChannel. If this method
	 * returns a non-<code>null</code> IChannelListener instance, the newly
	 * created channel must notify the given listener when channel events occur.
	 * If this method returns <code>null</code>, then no listener will be
	 * notified of channel events
	 * 
	 * @return IChannelListener to use for notification of received channel
	 *         events. If <code>null</code>, then no listener will be
	 *         notified of channel events.
	 * 
	 */
	public IChannelListener getListener();

	/**
	 * Get properties for new channel creation
	 * 
	 * @return Map with properties for new channel
	 */
	public Map getProperties();
}
