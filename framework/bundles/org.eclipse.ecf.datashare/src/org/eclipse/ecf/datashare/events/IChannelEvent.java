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
 * Super interface for events delivered to IChannel instances
 * 
 */
public interface IChannelEvent {
	/**
	 * Get the id of the channel associated with this event
	 * 
	 * @return ID of the channel. Will not be <code>null</code>.
	 */
	public ID getChannelID();
}
