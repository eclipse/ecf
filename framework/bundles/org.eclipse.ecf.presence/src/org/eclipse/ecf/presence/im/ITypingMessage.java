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

import org.eclipse.ecf.presence.IIMMessage;

/**
 * Typing message. This object represents information about a using typing
 * during chat.
 */
public interface ITypingMessage extends IIMMessage {

	/**
	 * Indicates whether remote user is actually typing.
	 * 
	 * @return true if currently typing, false if currently stopped.
	 */
	public boolean isTyping();

	/**
	 * Get the contents of the typing
	 * 
	 * @return String contents of the typing. May return <code>null</code>.
	 */
	public String getBody();

}
