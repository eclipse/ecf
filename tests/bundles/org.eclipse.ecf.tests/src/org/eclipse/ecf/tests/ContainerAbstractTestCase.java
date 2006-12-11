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

package org.eclipse.ecf.tests;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;

public abstract class ContainerAbstractTestCase extends ECFAbstractTestCase {

	protected String genericServerName = "ecf.generic.server";

	protected String genericClientName = "ecf.generic.client";

	protected String genericServerIdentity = "ecftcp://localhost:2222/server";

	protected IContainer server;

	protected IContainer[] clients;

	protected int clientCount = 1;
	
	protected ID serverID;
	
	protected IContainer getServer() {
		return server;
	}

	protected IContainer[] getClients() {
		return clients;
	}

	protected String getServerContainerName() {
		return genericServerName;
	}

	protected String getClientContainerName() {
		return genericClientName;
	}
	
	protected String getServerIdentity() {
		return genericServerIdentity;
	}

	protected ID getServerID() {
		return serverID;
	}
	
	protected int getClientCount() {
		return clientCount;
	}
	
	protected ID createServerID() throws Exception {
		return IDFactory.getDefault().createStringID(getServerIdentity());
	}
	
	protected IContainer createServer() throws Exception {
		return ContainerFactory.getDefault().createContainer(
				getServerContainerName(), new Object[] { getServerID() });
	}

	protected IContainer[] createClients() throws Exception {
		IContainer [] result = new IContainer[getClientCount()];
		for(int i=0; i < result.length; i++) {
			result[i] = createClient(i);
		}
		return result;
	}
	
	protected IContainer createClient(int index) throws Exception {
		return ContainerFactory.getDefault().createContainer(getClientContainerName());
	}

	protected void createServerAndClients() throws Exception {
		serverID = createServerID();
		server = createServer();
		clients = createClients();
	}

	protected void cleanUpClients() {
		if (clients != null) {
			for(int i=0; i < clients.length; i++) {
				clients[i].disconnect();
				clients[i].dispose();
				clients[i] = null;
			}
			clients = null;
		}
	}
	
	protected void cleanUpServerAndClients() {
		serverID = null;
		server.disconnect();
		server.dispose();
		server = null;
		cleanUpClients();
	}
	
	protected void connectClients() throws Exception {
		IContainer [] clients = getClients();
		for(int i=0; i < clients.length; i++) {
			clients[i].connect(getServerID(), null);
		}
	}
	
	protected void disconnectClients() throws Exception {
		IContainer [] clients = getClients();
		for(int i=0; i < clients.length; i++) {
			clients[i].disconnect();
		}
	}

	protected void assertHasEvent(Collection collection, Class eventType) {
		assertHasEventCount(collection, eventType, 1);
	}
	
	protected void assertHasEventCount(Collection collection, Class eventType, int eventCount) {
		int count = 0;
		for(Iterator i=collection.iterator(); i.hasNext(); ) {
			Object o = i.next();
			if (eventType.isInstance(o)) count++;
		}
		assertTrue(count == eventCount);
	}
	
	protected void assertHasMoreThanEventCount(Collection collection, Class eventType, int eventCount) {
		int count = 0;
		for(Iterator i=collection.iterator(); i.hasNext(); ) {
			Object o = i.next();
			if (eventType.isInstance(o)) count++;
		}
		assertTrue(count > eventCount);
	}

}
