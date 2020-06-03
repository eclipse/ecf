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
package org.eclipse.ecf.tests.osgi.services.distribution.r_osgi;


import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.tests.osgi.services.distribution.AbstractRemoteServiceRegisterTest;
import org.osgi.framework.ServiceReference;


public class R_OSGiRemoteServiceRegisterTest extends AbstractRemoteServiceRegisterTest {

	private static final String CONTAINER_TYPE_NAME = "ecf.r_osgi.peer";
	
	private static final String SERVER_IDENTITY = "r-osgi://localhost:9278";
	
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
				"r-osgi://localhost:"+(9279+index)) });
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
	
	public ServiceReference getReference() {
		// TODO Auto-generated method stub
		return null;
	}


	protected String getServerContainerTypeName() {
		return CONTAINER_TYPE_NAME;
	}
	
}
