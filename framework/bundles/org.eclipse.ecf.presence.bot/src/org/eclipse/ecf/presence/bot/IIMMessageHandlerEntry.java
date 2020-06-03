/****************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.presence.bot;

import org.eclipse.ecf.presence.im.IChatMessage;

/**
 * Contents for creating a im message handler.
 * 
 */
public interface IIMMessageHandlerEntry {

	public String getExpression();

	public IIMMessageHandler getHandler();

	public void handleIMMessage(IChatMessage message);

}
