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

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;

/**
 * 
 */
public abstract class AbstractConnectTest extends AbstractPresenceTestCase {

	public static final int CLIENT_COUNT = 2;
	public static final int SLEEPTIME = Integer.valueOf(System.getProperty(
			"org.eclipse.ecf.tests.presence.AbstractConnectTest.SLEEPTIME", "1000"))
			.intValue();

	protected void setUp() throws Exception {
		super.setUp();
		setClientCount(CLIENT_COUNT);
		clients = createClients();
	}

	public void testConnectOneClient() throws Exception {
		final int clientIndex = 0;
		final IContainer client = getClient(clientIndex);
		assertNull(client.getConnectedID());
		final ID serverConnectID = getServerConnectID(clientIndex);
		assertNotNull(serverConnectID);
		connectClient(client, serverConnectID, getConnectContext(clientIndex));
		assertEquals(serverConnectID, client.getConnectedID());
		sleep(SLEEPTIME);
		client.disconnect();
		assertNull(client.getConnectedID());
	}

	public void testConnectTwoClients() throws Exception {
		for (int i = 0; i < 2; i++) {
			final IContainer client = getClient(i);
			assertNull(client.getConnectedID());
			final ID serverConnectID = getServerConnectID(i);
			assertNotNull(serverConnectID);
			connectClient(client, serverConnectID, getConnectContext(i));
			assertEquals(serverConnectID, client.getConnectedID());
		}

		sleep(3000);

		for (int i = 0; i < 2; i++) {
			final IContainer client = getClient(i);
			client.disconnect();
			assertNull(client.getConnectedID());
		}
	}

}
