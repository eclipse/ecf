/*******************************************************************************
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.tests.osgi.services.distribution.r_osgi;


import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.tests.osgi.services.distribution.AbstractServiceRegisterTest;


public class R_OSGiServiceRegisterTest extends AbstractServiceRegisterTest {

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
		return ContainerFactory.getDefault().createContainer("ecf.r_osgi.peer",
				new Object[] { IDFactory.getDefault().createStringID(
				"r-osgi://localhost:"+(9279+index)) });
	}
	
	protected IContainer createServer() throws Exception {
		return ContainerFactory.getDefault().createContainer("ecf.r_osgi.peer",
				new Object[] { IDFactory.getDefault().createStringID(
				"r-osgi://localhost:9278") });
	}


	protected String getClientContainerName() {
		return "ecf.r_osgi.peer";
	}
	
}
