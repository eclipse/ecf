/****************************************************************************
 * Copyright (c) 2009 Jan S. Rellermeyer and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jan S. Rellermeyer - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.tests.osgi.services.distribution.impl;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.osgi.services.distribution.ServiceConstants;
import org.eclipse.ecf.tests.osgi.services.distribution.Activator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.discovery.ServicePublication;
import org.osgi.util.tracker.ServiceTracker;

import junit.framework.TestCase;

public class ServicePublicationTest extends TestCase {

	private IContainer container;

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		// JR: this should not be necessary
		final ID containerID = IDFactory.getDefault().createStringID(
				"r-osgi://localhost:9278");
		container = ContainerFactory.getDefault().createContainer(
				"ecf.r_osgi.peer", new Object[] { containerID });
	}

	protected void tearDown() throws Exception {
		container.dispose();
	}

	public void testServicePublication() throws InterruptedException {
		final BundleContext context = Activator.getDefault().getContext();

		// register a service with the marker property set
		final Dictionary props = new Hashtable();
		props.put(ServiceConstants.OSGI_REMOTE_INTERFACES,
				new String[] { TestServiceInterface1.class.getName() });
		// JR: this should not be necessary
		props
				.put(
						ServiceConstants.OSGI_REMOTE_CONFIGURATION_TYPE,
						new String[] { ServiceConstants.ECF_REMOTE_CONFIGURATION_TYPE });
		
		// prepare a service tracker
		final ServiceTracker tracker = new ServiceTracker(context, TestServiceInterface1.class.getName(), null);
		tracker.open();
		
		// register the (remote-enabled) service
		context.registerService(TestServiceInterface1.class.getName(),
				new TestService1(), props);

		// wait for service to become registered
		tracker.waitForService(5000);
		
		// expected behavior: an endpoint is published
		final ServiceReference ref = context.getServiceReference(ServicePublication.class.getName());
		assertTrue(ref != null);
	}


}
