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
package org.eclipse.ecf.tests.osgi.services.distribution.r_osgi.ws;


import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.tests.osgi.services.distribution.AbstractRemoteServiceRegisterTest;


public class R_OSGiWSRemoteServiceRegisterTest extends AbstractRemoteServiceRegisterTest {

	private static final String CONTAINER_TYPE_NAME = "ecf.r_osgi.peer.ws";
	
	private static final String SERVER_IDENTITY = "r-osgi.ws://localhost";
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		setClientCount(1);
		createServerAndClients();
		setupRemoteServiceAdapters();
	}

	
	protected void tearDown() throws Exception {
		cleanUpServerAndClients();
		super.tearDown();
	}

	protected int getClientCount() {
		return 0;
	}
	
	protected IContainer createServer() throws Exception {
		return ContainerFactory.getDefault().createContainer(CONTAINER_TYPE_NAME,SERVER_IDENTITY);
	}


	protected String getClientContainerName() {
		return CONTAINER_TYPE_NAME;
	}

	protected String getServerIdentity() {
		return SERVER_IDENTITY;
	}
	
	protected String getServerContainerTypeName() {
		return CONTAINER_TYPE_NAME;
	}
	
}
