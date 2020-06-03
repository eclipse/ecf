/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.presence;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.Event;

/**
 * Message event class. This event interface provides a wrapper for
 * {@link IIMMessage}s received from remotes.
 */
public interface IIMMessageEvent extends Event {

	/**
	 * Get the ID of the sender of the chat message.
	 * 
	 * @return ID of the sender of the message. Will not be <code>null</code>.
	 */
	public ID getFromID();

}
