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

package org.eclipse.ecf.presence.im;

import org.eclipse.ecf.presence.IIMMessageEvent;

/**
 * Typing message event.
 */
public interface ITypingMessageEvent extends IIMMessageEvent {

	/**
	 * Get the typing message ffrom this event object.
	 * 
	 * @return ITypingMessage that is the message associated with this event.
	 *         Will not be <code>null</code>.
	 */
	public ITypingMessage getTypingMessage();
}
