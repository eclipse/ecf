/*******************************************************************************
 * Copyright (c) 2009 Markus Alexander Kuppe.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Alexander Kuppe (ecf-dev_eclipse.org <at> lemmster <dot> de) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.tests.provider.discovery;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.events.IContainerEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.discovery.IServiceEvent;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.provider.discovery.CompositeDiscoveryContainer;
import org.eclipse.ecf.provider.discovery.CompositeServiceContainerEvent;
import org.eclipse.ecf.tests.discovery.DiscoveryServiceTest;
import org.eclipse.ecf.tests.discovery.listener.TestServiceListener;

public class CompositeDiscoveryServiceContainerTest extends
		DiscoveryServiceTest {

	private IContainer container;

	public CompositeDiscoveryServiceContainerTest() {
		super("ecf.discovery.composite");
		setComparator(new CompositeServiceInfoComporator());
		//TODO  jSLP currently has the longer rediscovery interval
		setWaitTimeForProvider(Long.parseLong(System.getProperty("net.slp.rediscover", new Long(60L * 1000L).toString()))); //$NON-NLS-1$);
		//TODO-mkuppe https://bugs.eclipse.org/bugs/show_bug.cgi?id=218308
		setScope(IServiceTypeID.DEFAULT_SCOPE[0]);
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.DiscoveryServiceTest#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		container = ContainerFactory.getDefault().createContainer(containerUnderTest);
		eventsToExpect = ((CompositeDiscoveryContainer) discoveryLocator).getDiscoveryContainers().size();
	}
	

	protected void addServiceListener(TestServiceListener serviceListener) {
		discoveryLocator.addServiceListener(serviceListener);
		addListenerRegisterAndWait(serviceListener, serviceInfo);
		discoveryLocator.removeServiceListener(serviceListener);
		IContainerEvent[] events = serviceListener.getEvent();
		assertNotNull("Test listener didn't receive any discovery events.", events);
		assertEquals("Test listener received unexpected amount of discovery events: \n\t" + Arrays.asList(events), eventsToExpect, events.length);
		Set origContainers = new HashSet();
		for (int i = 0; i < events.length; i++) {
			CompositeServiceContainerEvent event = (CompositeServiceContainerEvent) events[i];
			ID localContainerId = event.getLocalContainerID();
			ID connectedId = container.getConnectedID();
			assertTrue("Container mismatch, excepted:\n\t" + localContainerId + " but was:\n\t" + connectedId, localContainerId.equals(connectedId));
			IServiceInfo serviceInfo2 = ((IServiceEvent) event).getServiceInfo();
			assertTrue("IServiceInfo should match, expected:\n\t" + serviceInfo + " but was \n\t" + serviceInfo2, comparator.compare(serviceInfo2, serviceInfo) == 0);
			origContainers.add(event.getOriginalLocalContainerID());
		}
		assertEquals("A nested container didn't send an event, but another multiple", eventsToExpect, origContainers.size());
	}
}
