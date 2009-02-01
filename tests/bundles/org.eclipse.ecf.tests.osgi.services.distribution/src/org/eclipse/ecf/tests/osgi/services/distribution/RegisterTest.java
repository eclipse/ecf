/*******************************************************************************
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.tests.osgi.services.distribution;

import java.util.Properties;

import org.eclipse.ecf.osgi.services.distribution.ServiceConstants;
import org.eclipse.ecf.tests.remoteservice.IConcatService;
import org.osgi.framework.BundleContext;


public class RegisterTest extends AbstractDistributionTest implements ServiceConstants {

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		setClientCount(1);
		createServerAndClients();
		connectClients();
	}

	
	protected void tearDown() throws Exception {
		super.tearDown();
		cleanUpServerAndClients();
	}

	protected String getClientContainerName() {
		return "ecf.generic.client";
	}
	
	public void testRegister() throws Exception {
		BundleContext bc = Activator.getDefault().getContext();
		Properties props = new Properties();
		props.put(OSGI_REMOTE_INTERFACES, new String[] {OSGI_REMOTE_INTERFACES_WILDCARD});
		props.put(OSGI_REMOTE_CONFIGURATION_TYPE,new String[] { ECF_REMOTE_CONFIGURATION_TYPE });
		bc.registerService(new String[] { IConcatService.class.getName() }, createService(), props);
	}
	
}
