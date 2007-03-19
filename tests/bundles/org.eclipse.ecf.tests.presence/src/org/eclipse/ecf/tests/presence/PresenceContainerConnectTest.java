/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.tests.presence;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.security.IConnectContext;

/**
 * 
 */
public class PresenceContainerConnectTest extends PresenceAbstractTestCase {

	public void testConnectClient() throws Exception {
		IContainer client = getClients()[0];
		assertNull(client.getConnectedID());
		ID serverConnectID = getServerConnectID(client);
		assertNotNull(serverConnectID);
		IConnectContext connectContext = getConnectContext(client);
		connectClient(client, serverConnectID, connectContext);
		assertEquals(serverConnectID, client.getConnectedID());
		sleep(3000);
		client.disconnect();
		assertNull(client.getConnectedID());
	}

	public void testConnectTwoClients() throws Exception {
		IContainer clientone = getClients()[0];
		assertNull(clientone.getConnectedID());
		ID serverConnectID = getServerConnectID(clientone);
		assertNotNull(serverConnectID);
		IConnectContext connectContext = getConnectContext(clientone);
		connectClient(clientone, serverConnectID, connectContext);
		assertEquals(serverConnectID, clientone.getConnectedID());

		IContainer clienttwo = getClients()[1];
		assertNull(clienttwo.getConnectedID());
		serverConnectID = getServerConnectID(clienttwo);
		assertNotNull(serverConnectID);
		connectContext = getConnectContext(clienttwo);
		connectClient(clienttwo, serverConnectID, connectContext);
		assertEquals(serverConnectID, clienttwo.getConnectedID());
		sleep(3000);

		clientone.disconnect();
		assertNull(clientone.getConnectedID());
		clienttwo.disconnect();
		assertNull(clienttwo.getConnectedID());
	}

}
