/****************************************************************************
 * Copyright (c) 2004, 2009 Composent, Inc., Peter Nehrer, Boris Bokowski.
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

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;

/**
 * Event delivered to an IChannelListener when the parent container of a channel
 * connects successfully to a target ID via the {@link IContainer#connect(ID, org.eclipse.ecf.core.security.IConnectContext) connect(ID, IConnectContext)} method.
 */
public interface IChannelConnectEvent extends IChannelEvent {
	/**
	 * Get ID of target IContainer that connected.
	 * 
	 * @return ID of IContainer that has connected. Will not be <code>null</code>.
	 */
	public ID getTargetID();
}
