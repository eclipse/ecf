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

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.remoteservice.Constants;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.tests.remoteservice.AbstractRemoteServiceTest;
import org.eclipse.ecf.tests.remoteservice.Activator;
import org.eclipse.ecf.tests.remoteservice.IConcatService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class TransparentProxyTest extends AbstractRemoteServiceTest {

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
			adapters[i + 1] = (IRemoteServiceContainerAdapter) getClients()[i].getAdapter(IRemoteServiceContainerAdapter.class);
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

	/**
	 * TODO: can be removed in favor of extending
	 * org.eclipse.ecf.tests.remoteservice.generic.TransparentProxyTest or
	 * something similar, if the dependency on the clients array can be avoided
	 * and the getRemoteServiceAdapters() method can be used instead (as I do it
	 * in here).
	 * 
	 * @throws Exception
	 */
	public void testTransparentProxy() throws Exception {
		final IRemoteServiceContainerAdapter[] adapters = getRemoteServiceAdapters();
		// adapter[0] is the service 'server'
		// adapter[1] is the service target (client)
		final Dictionary props = new Hashtable();
		props.put(Constants.SERVICE_REGISTRATION_TARGETS, ((IContainer) getRemoteServiceAdapters()[1]).getConnectedID());
		props.put(Constants.AUTOREGISTER_REMOTE_PROXY, "true");
		// Register
		adapters[0].registerRemoteService(new String[] {IConcatService.class.getName()}, createService(), props);
		// Give some time for propagation
		sleep(3000);

		final BundleContext bc = Activator.getDefault().getContext();
		assertNotNull(bc);
		final ServiceReference ref = bc.getServiceReference(IConcatService.class.getName());
		assertNotNull(ref);
		final IConcatService concatService = (IConcatService) bc.getService(ref);
		assertNotNull(concatService);
		System.out.println("proxy call start");
		final String result = concatService.concat("OSGi ", "is cool");
		System.out.println("proxy call end. result=" + result);
		sleep(3000);
		bc.ungetService(ref);
		sleep(3000);
	}
	
	protected void addRemoteServiceListeners() {
		for (int i = 0; i < adapters.length; i++) {
			adapters[i].addRemoteServiceListener(createRemoteServiceListener(true));
		}
	}
}
