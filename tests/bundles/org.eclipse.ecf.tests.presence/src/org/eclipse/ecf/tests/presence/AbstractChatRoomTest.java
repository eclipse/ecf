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

package org.eclipse.ecf.tests.presence;

import org.eclipse.ecf.presence.chatroom.IChatRoomInfo;
import org.eclipse.ecf.presence.chatroom.IChatRoomManager;

/**
 * 
 */
public abstract class AbstractChatRoomTest extends AbstractPresenceTestCase {

	IChatRoomManager chatRoomManager = null;
	public static final int WAITTIME = 3000;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.tests.presence.AbstractPresenceTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		setClientCount(1);
		clients = createClients();
		chatRoomManager = getPresenceAdapter(0).getChatRoomManager();
		for (int i = 0; i < 1; i++) {
			connectClient(i);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		disconnectClients();
	}

	public void testGetChatRoomInfos() throws Exception {
		IChatRoomInfo[] chatRoomInfos = chatRoomManager.getChatRoomInfos();
		assertNotNull(chatRoomInfos);
	}

}
