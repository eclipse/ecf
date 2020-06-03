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

package org.eclipse.ecf.presence.bot;

import org.eclipse.ecf.presence.im.IChatMessage;

/**
 * Message handler for receiving a IM message.
 */
public interface IIMMessageHandler extends IContainerAdvisor {

	/**
	 * Initialize robot with robot entry data.
	 * 
	 * @param robot
	 *            the robot to initialize. Will not be <code>null</code>.
	 */
	public void init(IIMBotEntry robot);

	/**
	 * This method is called when a {@link IChatMessage} is received.
	 * 
	 * @param message
	 *            the {@link IChatMessage} received. Will not be
	 *            <code>null</code>. Implementers should not block the
	 *            calling thread. Any methods on the given <code>message</code>
	 *            parameter may be called.
	 */
	public void handleIMMessage(IChatMessage message);

}
