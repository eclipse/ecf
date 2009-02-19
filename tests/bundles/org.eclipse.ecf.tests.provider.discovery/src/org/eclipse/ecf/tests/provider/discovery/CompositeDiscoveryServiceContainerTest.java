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

import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.tests.discovery.DiscoveryServiceTest;

public class CompositeDiscoveryServiceContainerTest extends
		DiscoveryServiceTest {

	public CompositeDiscoveryServiceContainerTest() {
		super("ecf.discovery.composite");
		setComparator(new CompositeServiceInfoComporator());
		//TODO  jSLP currently has the longer rediscovery interval
		setWaitTimeForProvider(Long.parseLong(System.getProperty("net.slp.rediscover", new Long(60L * 1000L).toString()))); //$NON-NLS-1$);
		//TODO-mkuppe https://bugs.eclipse.org/bugs/show_bug.cgi?id=218308
		setScope(IServiceTypeID.DEFAULT_SCOPE[0]);
	}
//
//	protected void addServiceListener(TestServiceListener serviceListener) {
//		discoveryContainer.addServiceListener(serviceListener);
//		addListenerRegisterAndWait(serviceListener, serviceInfo);
//		discoveryContainer.removeServiceListener(serviceListener);
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
