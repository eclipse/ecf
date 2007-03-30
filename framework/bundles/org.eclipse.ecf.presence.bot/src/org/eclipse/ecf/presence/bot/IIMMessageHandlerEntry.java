/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.presence.bot;

import org.eclipse.ecf.presence.im.IChatMessage;

public interface IIMMessageHandlerEntry {

	public String getExpression();

	public IIMMessageHandler getHandler();

	public void handleIMMessage(IChatMessage message);

}
