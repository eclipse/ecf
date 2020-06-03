/****************************************************************************
 * Copyright (c) 2015 Composent, Inc. and others.
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
package org.eclipse.ecf.tests.osgi.services.distribution.r_osgi.ws;


import java.util.Properties;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.osgi.services.distribution.IDistributionConstants;
import org.eclipse.ecf.tests.osgi.services.distribution.AbstractRemoteServiceAccessTest;


public class R_OSGiWSRemoteServiceAccessTest extends AbstractRemoteServiceAccessTest {

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

	protected IContainer createClient(int index) throws Exception {
		return ContainerFactory.getDefault().createContainer(CONTAINER_TYPE_NAME,
				new Object[] { IDFactory.getDefault().createStringID(
				"r-osgi.ws://localhost:"+(9279+index)) });
	}
	
	protected IContainer createServer() throws Exception {
		serverID = IDFactory.getDefault().createID("ecf.namespace.r_osgi.ws",SERVER_IDENTITY);
		return ContainerFactory.getDefault().createContainer(CONTAINER_TYPE_NAME,serverID);
	}

	protected String getClientContainerName() {
		return CONTAINER_TYPE_NAME;
	}

	protected String getServerIdentity() {
		return SERVER_IDENTITY;
	}
	
	protected String getServerContainerName() {
		return CONTAINER_TYPE_NAME;
	}
	
	protected Properties getServiceProperties() {
		Properties props = super.getServiceProperties();
		props.put(IDistributionConstants.SERVICE_EXPORTED_CONTAINER_ID, serverID);
		return props;
	}
}
