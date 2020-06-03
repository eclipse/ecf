/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
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

package org.eclipse.ecf.tests.provider.xmpp;

import org.eclipse.ecf.tests.presence.AbstractChatRoomInvitationTest;

/**
 *
 */
public class ChatRoomInvitationTest extends AbstractChatRoomInvitationTest {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.tests.presence.AbstractPresenceTestCase#
	 * getClientContainerName()
	 */
	protected String getClientContainerName() {
		return XMPP.CONTAINER_NAME;
	}

	protected void tearDown() throws Exception {
		// This is a possible workaround for what appears to be Smack bug:
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=321032
		Thread.sleep(2000);
		super.tearDown();
	}
}
