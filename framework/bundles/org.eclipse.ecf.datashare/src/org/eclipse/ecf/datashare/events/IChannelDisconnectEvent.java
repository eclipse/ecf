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
 * Event delivered to the IChannelListener when the parent container of a channel disconnects.
 */
public interface IChannelDisconnectEvent extends IChannelEvent {
	/**
	 * Get ID of IContainer that has disconnected from channel.
	 * 
	 * @return ID of IContainer that has disconnected. Will not be <code>null</code>.
	 */
	public ID getTargetID();
}
