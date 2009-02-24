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

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.osgi.services.discovery.ECFServicePublication;
import org.eclipse.ecf.osgi.services.distribution.ServiceConstants;
import org.eclipse.ecf.remoteservice.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.discovery.ServicePublication;
import org.osgi.util.tracker.ServiceTracker;


public class GenericRemoteServiceRegisterTest extends AbstractDistributionTest implements ServiceConstants, ECFServicePublication {

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
		setupRemoteServiceAdapters();
	}

	
	protected void tearDown() throws Exception {
		super.tearDown();
		cleanUpServerAndClients();
	}

	protected String getClientContainerName() {
		return "ecf.generic.client";
	}
	

	public void testRegisterAllContainers() throws Exception {
		Properties props = new Properties();
		props.put(OSGI_REMOTE_INTERFACES, new String[] {OSGI_REMOTE_INTERFACES_WILDCARD});
		registerDefaultService(props);
		Thread.sleep(3000);
	}
	
	public void testRegisterServerContainer() throws Exception {
		Properties props = new Properties();
		props.put(OSGI_REMOTE_INTERFACES, new String[] {OSGI_REMOTE_INTERFACES_WILDCARD});
		IContainer serverContainer = getServer();
		props.put(Constants.SERVICE_CONTAINER_ID, serverContainer.getID());
		registerDefaultService(props);
		Thread.sleep(3000);
	}
	
	public void testRegisterServicePublication() throws Exception {
		// First set up service tracker for ServicePublication
		ServiceTracker servicePublicationTracker = new ServiceTracker(getContext(), ServicePublication.class.getName(), null);
		servicePublicationTracker.open();
		
		Properties props = new Properties();
		props.put(OSGI_REMOTE_INTERFACES, new String[] {OSGI_REMOTE_INTERFACES_WILDCARD});
		IContainer serverContainer = getServer();
		props.put(Constants.SERVICE_CONTAINER_ID, serverContainer.getID());
		ServiceRegistration reg = registerDefaultService(props);

		Thread.sleep(3000);
		
		// Now get ServicePublications from tracker
		ServiceReference [] servicePublicationSRs = servicePublicationTracker.getServiceReferences();
		List servicePublicationSRsList = Arrays.asList(servicePublicationSRs);
		assertTrue(servicePublicationSRs != null);
	}
	
}
