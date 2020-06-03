/****************************************************************************
 * Copyright (c) 2009 EclipseSource and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.server.generic;

import junit.framework.TestCase;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.server.generic.SimpleGenericServer;

public class GenericServerTest extends TestCase {

	private static final int SERVER_PORT = 5555;
	private static final String SERVER_PATH = "/server";
	private static final int SERVER_KEEPALIVE = 30000;
	
	SimpleGenericServer server;
	
	protected void setUp() throws Exception {
		super.setUp();
		server = new SimpleGenericServer("localhost",SERVER_PORT);
		server.start(SERVER_PATH, SERVER_KEEPALIVE);
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		server.stop();
		server = null;
	}
	public void testGetRemoteServiceContainerAdapter() throws Exception {
		IContainer container = server.getFirstServerContainer();
		assertNotNull(container);
		IRemoteServiceContainerAdapter adapter = (IRemoteServiceContainerAdapter) container.getAdapter(IRemoteServiceContainerAdapter.class);
		assertNotNull(adapter);
	}
}
