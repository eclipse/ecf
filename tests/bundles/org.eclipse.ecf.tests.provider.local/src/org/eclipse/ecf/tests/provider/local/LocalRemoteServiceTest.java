/****************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.provider.local;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceID;
import org.eclipse.ecf.tests.remoteservice.AbstractRemoteServiceTest;

public class LocalRemoteServiceTest extends AbstractRemoteServiceTest {

	protected String getClientContainerName() {
		return Local.CONTAINER_FACTORY_NAME;
	}

	protected String getServerContainerName() {
		return Local.CONTAINER_FACTORY_NAME;
	}

	protected ID createServerID() throws Exception {
		return IDFactory.getDefault().createGUID();
	}

	protected void setupRemoteServiceAdapters() throws Exception {
		for (int i = 0; i < 2; i++) {
			adapters[i] = (IRemoteServiceContainerAdapter) server
					.getAdapter(IRemoteServiceContainerAdapter.class);
		}
	}

	protected void setUp() throws Exception {
		super.setUp();
		setClientCount(1);
		createServerAndClients();
		adapters = new IRemoteServiceContainerAdapter[2];
		ids = new IRemoteServiceID[2];
		setupRemoteServiceAdapters();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		if (server != null) {
			serverID = null;
			server.dispose();
			server = null;
		}
	}
	
	protected IContainer createServer() throws Exception {
		server = getContainerFactory().createContainer(Local.CONTAINER_FACTORY_NAME);
		return server;
	}
	
	protected IContainer createClient(int index) throws Exception {
		return server;
	}
}
