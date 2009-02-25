/*******************************************************************************
 * Copyright (c) 2008 Jan S. Rellermeyer, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan S. Rellermeyer - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.tests.remoteservice.r_osgi;

import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.tests.remoteservice.AbstractRemoteServiceTest;

/**
 * Remote service test, adapted for the R-OSGi provider setup.
 * 
 * @author Jan S. Rellermeyer, ETH Zurich
 */
public class RemoteServiceTest extends AbstractRemoteServiceTest {

	/**
	 * 
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		setClientCount(1);
		createServerAndClients();
		connectClients();
		setupRemoteServiceAdapters();
		addRemoteServiceListeners();
	}

	/**
	 * 
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		cleanUpServerAndClients();
		super.tearDown();
	}

	/**
	 * set up the adapters. adapters[0] is the one on which services will be
	 * registered. adapters[1] is the one which will import the service and
	 * generate a proxy.
	 */
	protected void setupRemoteServiceAdapters() throws Exception {
		adapters = new IRemoteServiceContainerAdapter[clientCount + 1];
		adapters[0] = (IRemoteServiceContainerAdapter) getServer();
		final int clientCount = getClientCount();
		for (int i = 0; i < clientCount; i++) {
			adapters[i + 1] = (IRemoteServiceContainerAdapter) getClients()[i]
					.getAdapter(IRemoteServiceContainerAdapter.class);
		}
	}

	/**
	 * 
	 * @see org.eclipse.ecf.tests.ContainerAbstractTestCase#getServerIdentity()
	 */
	protected String getServerIdentity() {
		return R_OSGi.SERVER_IDENTITY;
	}

	/**
	 * 
	 * @see org.eclipse.ecf.tests.ContainerAbstractTestCase#getServerContainerName()
	 */
	protected String getServerContainerName() {
		return R_OSGi.CLIENT_CONTAINER_NAME;
	}

	/**
	 * 
	 * @see org.eclipse.ecf.tests.remoteservice.AbstractRemoteServiceTest#getClientContainerName()
	 */
	protected String getClientContainerName() {
		return R_OSGi.CLIENT_CONTAINER_NAME;
	}

	protected void addRemoteServiceListeners() {
		for (int i = 0; i < adapters.length; i++) {
			adapters[i].addRemoteServiceListener(createRemoteServiceListener(true));
		}
	}
	
}
