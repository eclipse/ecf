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

import java.io.Serializable;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ecf.core.identity.ID;

/**
 * Super interface for messages. Sub interfaces define specific types of
 * messages...for example chat, user keyboard activity/typing and chat room
 * messages.
 */
public interface IIMMessage extends IAdaptable, Serializable {

	/**
	 * Get ID of originator of message.
	 * 
	 * @return ID of originator of message. Will not be <code>null</code>.
	 */
	public ID getFromID();

}
