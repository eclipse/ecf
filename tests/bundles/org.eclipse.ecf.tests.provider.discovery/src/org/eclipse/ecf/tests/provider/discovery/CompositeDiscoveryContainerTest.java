/*******************************************************************************
 * Copyright (c) 2009 Markus Alexander Kuppe.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Markus Alexander Kuppe (ecf-dev_eclipse.org <at> lemmster <dot> de) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.tests.provider.discovery;

import java.util.List;

import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.util.ECFRuntimeException;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.provider.discovery.CompositeDiscoveryContainer;
import org.eclipse.ecf.tests.discovery.DiscoveryContainerTest;

public class CompositeDiscoveryContainerTest extends DiscoveryContainerTest {

	private TestDiscoveryContainer testDiscoveryContainer;

	public CompositeDiscoveryContainerTest() {
		super("ecf.discovery.*");
		setComparator(new CompositeServiceInfoComporator());
		//TODO  jSLP currently has the longer rediscovery interval
		setWaitTimeForProvider(Long.parseLong(System.getProperty("net.slp.rediscover", new Long(60L * 1000L).toString()))); //$NON-NLS-1$);
		//TODO-mkuppe https://bugs.eclipse.org/bugs/show_bug.cgi?id=218308
		setScope(IServiceTypeID.DEFAULT_SCOPE[0]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.DiscoveryTest#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		eventsToExpect = ((CompositeDiscoveryContainer) discoveryLocator).getDiscoveryContainers().size();
	}

	/**
	 * Check if 
	 * @throws ContainerConnectException 
	 */
	public void testAddContainerWithRegisteredServices() throws ContainerConnectException {
		try {
			try {
				discoveryAdvertiser.registerService(serviceInfo);
			} catch (ECFRuntimeException e) {
				fail("Registering a service failed on a new IDCA");
			}
			CompositeDiscoveryContainer cdc = (CompositeDiscoveryContainer) discoveryLocator;
			testDiscoveryContainer = new TestDiscoveryContainer();
			assertTrue(cdc.addContainer(testDiscoveryContainer));
			List registeredServices = testDiscoveryContainer.getRegisteredServices();
			assertEquals("registerService(aService) wasn't called on TestDiscoveryContainer", serviceInfo, registeredServices.get(0));
		} finally {
			CompositeDiscoveryContainer cdc = (CompositeDiscoveryContainer) discoveryLocator;
			assertTrue(cdc.removeContainer(testDiscoveryContainer));
		}
	}

	public void testAddContainerWithoutRegisteredServices() throws ContainerConnectException {
		try {
			try {
				discoveryAdvertiser.registerService(serviceInfo);
				discoveryAdvertiser.unregisterService(serviceInfo);
			} catch (ECFRuntimeException e) {
				fail("Re-/Unregistering a service failed on a new IDCA");
			}
			CompositeDiscoveryContainer cdc = (CompositeDiscoveryContainer) discoveryLocator;
			testDiscoveryContainer = new TestDiscoveryContainer();
			assertTrue(cdc.addContainer(testDiscoveryContainer));
			List registeredServices = testDiscoveryContainer.getRegisteredServices();
			assertTrue(registeredServices.isEmpty());
		} finally {
			CompositeDiscoveryContainer cdc = (CompositeDiscoveryContainer) discoveryLocator;
			assertTrue(cdc.removeContainer(testDiscoveryContainer));
		}
	}

//	protected void addServiceListener(TestServiceListener serviceListener) {
//		discoveryLocator.addServiceListener(serviceListener);
//		addListenerRegisterAndWait(serviceListener, serviceInfo);
//		discoveryLocator.removeServiceListener(serviceListener);
//		IContainerEvent[] events = serviceListener.getEvent();
//		assertNotNull("Test listener didn't receive discovery", events);
//		assertEquals("Test listener received more than expected discovery event", eventsToExpect, events.length);
//		Set origContainers = new HashSet();
//		for (int i = 0; i < events.length; i++) {
//			CompositeServiceContainerEvent event = (CompositeServiceContainerEvent) events[i];
//			assertTrue("Container mismatch", event.getLocalContainerID().equals(container.getConnectedID()));
//			assertTrue("IServiceInfo mismatch", comparator.compare(((IServiceEvent) event).getServiceInfo(), serviceInfo) == 0);
//			origContainers.add(event.getOriginalLocalContainerID());
//		}
//		assertEquals("A nested container didn't send an event, but another multiple", eventsToExpect, origContainers.size());
//	}
}
