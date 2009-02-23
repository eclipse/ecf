/****************************************************************************
 * Copyright (c) 2009 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *    Markus Alexander Kuppe (ecf-dev_eclipse.org <at> lemmster <dot> de) - Reworked completely
 *****************************************************************************/

package org.eclipse.ecf.tests.discovery;


import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.util.ECFRuntimeException;
import org.eclipse.ecf.discovery.IServiceEvent;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.tests.discovery.listener.TestServiceListener;
import org.eclipse.ecf.tests.discovery.listener.TestServiceTypeListener;

public abstract class DiscoveryTest extends AbstractDiscoveryTest {

	public DiscoveryTest(String name) {
		super(name);
	}
	
	protected void registerService(IServiceInfo serviceInfo) throws Exception {
		assertNotNull(serviceInfo);
		assertNotNull(discoveryContainer);
		discoveryContainer.registerService(serviceInfo);
	}

	protected void unregisterService(IServiceInfo serviceInfo) throws Exception {
		assertNotNull(serviceInfo);
		assertNotNull(discoveryContainer);
		discoveryContainer.unregisterService(serviceInfo);
	}

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		discoveryContainer.unregisterService(serviceInfo);
		container.disconnect();
		container.dispose();
		super.tearDown();
	}

	protected void registerService() {
		try {
			discoveryContainer.registerService(serviceInfo);
		} catch (final ECFRuntimeException e) {
			fail("IServiceInfo may be valid with this IDCA");
		}
	}

	protected void unregisterService() {
		try {
			discoveryContainer.unregisterService(serviceInfo);
		} catch (final ECFRuntimeException e) {
			fail("unregistering of " + serviceInfo + " should just work");
		}
	}

	protected void addListenerRegisterAndWait(TestServiceListener testServiceListener, IServiceInfo aServiceInfo) {
		synchronized (testServiceListener) {
			// register a service which we expect the test listener to get notified of
			registerService();
			try {
				testServiceListener.wait(waitTimeForProvider);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				fail("Some discovery unrelated threading issues?");
			}
		}
	}

	protected void addServiceListener(TestServiceListener serviceListener) {
		discoveryContainer.addServiceListener(serviceListener);
		addListenerRegisterAndWait(serviceListener, serviceInfo);
		discoveryContainer.removeServiceListener(serviceListener);
		assertNotNull("Test listener didn't receive discovery", serviceListener.getEvent());
		assertEquals("Test listener received more than expected discovery event", eventsToExpect, serviceListener.getEvent().length);
		assertTrue("Container mismatch", serviceListener.getEvent()[eventsToExpect - 1].getLocalContainerID().equals(container.getConnectedID()));
		assertTrue("IServiceInfo mismatch", comparator.compare(((IServiceEvent) serviceListener.getEvent()[eventsToExpect - 1]).getServiceInfo(), serviceInfo) == 0);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServiceInfo(org.eclipse.ecf.discovery.identity.IServiceID)}.
	 * @throws ContainerConnectException 
	 */
	public void testGetServiceInfo() throws ContainerConnectException {
		container.connect(null, null);
		registerService();
		final IServiceInfo info = discoveryContainer.getServiceInfo(serviceInfo.getServiceID());
		assertTrue("IServiceInfo should match, expected:\n" + serviceInfo + " but:\n" + info, comparator.compare(info, serviceInfo) == 0);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServiceTypes()}.
	 * @throws ContainerConnectException 
	 */
	public void testGetServiceTypes() throws ContainerConnectException {
		container.connect(null, null);
		registerService();
		final IServiceTypeID[] serviceTypeIDs = discoveryContainer.getServiceTypes();
		assertTrue(serviceTypeIDs.length > 0);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServices()}.
	 * @throws ContainerConnectException 
	 */
	public void testGetServices() throws ContainerConnectException {
		container.connect(null, null);
		registerService();
		final IServiceInfo[] services = discoveryContainer.getServices();
		assertTrue(services.length >= 1);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServices(org.eclipse.ecf.discovery.identity.IServiceTypeID)}.
	 * @throws ContainerConnectException 
	 */
	public void testGetServicesIServiceTypeID() throws ContainerConnectException {
		container.connect(null, null);
		registerService();
		final IServiceInfo serviceInfos[] = discoveryContainer.getServices(serviceInfo.getServiceID().getServiceTypeID());
		assertTrue(serviceInfos.length > 0);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#registerService(org.eclipse.ecf.discovery.IServiceInfo)}.
	 * @throws ContainerConnectException 
	 */
	public void testRegisterService() throws ContainerConnectException {
		container.connect(null, null);
		registerService();
		final IServiceInfo[] services = discoveryContainer.getServices();
		assertTrue(services.length >= 1);
		for (int i = 0; i < services.length; i++) {
			final IServiceInfo service = services[i];
			if (comparator.compare(service, serviceInfo) == 0) {
				return;
			}
		}
		fail("Self registered service not found");
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#unregisterService(org.eclipse.ecf.discovery.IServiceInfo)}.
	 * @throws ContainerConnectException 
	 */
	public void testUnregisterService() throws ContainerConnectException {
		testRegisterService();
		unregisterService();
		final IServiceInfo[] services = discoveryContainer.getServices();
		for (int i = 0; i < services.length; i++) {
			final IServiceInfo service = services[i];
			if (comparator.compare(service, serviceInfo) == 0) {
				fail("Expected service to be not registered anymore");
			}
		}
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#addServiceListener(org.eclipse.ecf.discovery.IServiceListener)}.
	 * @throws ContainerConnectException 
	 */
	public void testAddServiceListenerIServiceListener() throws ContainerConnectException {
		container.connect(null, null);
		assertTrue("No Services must be registerd at this point", discoveryContainer.getServices().length == 0);
		final TestServiceListener tsl = new TestServiceListener(eventsToExpect);
		addServiceListener(tsl);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#addServiceListener(org.eclipse.ecf.discovery.identity.IServiceTypeID, org.eclipse.ecf.discovery.IServiceListener)}.
	 * @throws ContainerConnectException 
	 */
	public void testAddServiceListenerIServiceTypeIDIServiceListener() throws ContainerConnectException {
		container.connect(null, null);
		assertTrue("No Services must be registerd at this point", discoveryContainer.getServices().length == 0);

		final TestServiceListener tsl = new TestServiceListener(eventsToExpect);
		discoveryContainer.addServiceListener(serviceInfo.getServiceID().getServiceTypeID(), tsl);
		addListenerRegisterAndWait(tsl, serviceInfo);
		discoveryContainer.removeServiceListener(serviceInfo.getServiceID().getServiceTypeID(), tsl);
		
		assertNotNull("Test listener didn't receive discovery", tsl.getEvent());
		assertEquals("Test listener received more than expected discovery event", eventsToExpect, tsl.getEvent().length);
		assertTrue("Container mismatch", tsl.getEvent()[eventsToExpect - 1].getLocalContainerID().equals(container.getConnectedID()));
		assertTrue("IServiceInfo mismatch", comparator.compare(((IServiceEvent) tsl.getEvent()[eventsToExpect - 1]).getServiceInfo(), serviceInfo) == 0);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#addServiceTypeListener(org.eclipse.ecf.discovery.IServiceTypeListener)}.
	 * @throws ContainerConnectException 
	 */
	public void testAddServiceTypeListener() throws ContainerConnectException {
		container.connect(null, null);
		assertTrue("No Services must be registerd at this point", discoveryContainer.getServices().length == 0);

		final TestServiceTypeListener testTypeListener = new TestServiceTypeListener(eventsToExpect);
		discoveryContainer.addServiceTypeListener(testTypeListener);

		synchronized (testTypeListener) {
			// register a service which we expect the test listener to get notified of
			registerService();
			try {
				testTypeListener.wait(waitTimeForProvider);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				fail("Some discovery unrelated threading issues?");
			}
		}
		
		discoveryContainer.removeServiceTypeListener(testTypeListener);
		
		assertNotNull("Test listener didn't receive discovery", testTypeListener.getEvent());
		assertEquals("Test listener received more than expected discovery event", eventsToExpect, testTypeListener.getEvent().length);
		assertTrue("Container mismatch", testTypeListener.getEvent()[eventsToExpect - 1].getLocalContainerID().equals(container.getConnectedID()));
	}
	

	/**
	 * Test method for
	 * {@link org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#removeServiceListener(org.eclipse.ecf.discovery.IServiceListener)}.
	 * @throws ContainerConnectException 
	 */
	public void testRemoveServiceListenerIServiceListener() throws ContainerConnectException {
		container.connect(null, null);
		final TestServiceListener serviceListener = new TestServiceListener(eventsToExpect);
		addServiceListener(serviceListener);
		//TODO reregister and verify the listener doesn't receive any events any longer.
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#removeServiceListener(org.eclipse.ecf.discovery.identity.IServiceTypeID, org.eclipse.ecf.discovery.IServiceListener)}.
	 * @throws ContainerConnectException 
	 */
	public void testRemoveServiceListenerIServiceTypeIDIServiceListener() throws ContainerConnectException {
		container.connect(null, null);
		final TestServiceListener serviceListener = new TestServiceListener(eventsToExpect);
		addServiceListener(serviceListener);
		//TODO reregister and verify the listener doesn't receive any events any longer.
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#removeServiceTypeListener(org.eclipse.ecf.discovery.IServiceTypeListener)}.
	 * @throws ContainerConnectException 
	 */
	public void testRemoveServiceTypeListener() throws ContainerConnectException {
		container.connect(null, null);
		assertTrue("No Services must be registerd at this point", discoveryContainer.getServices().length == 0);

		final TestServiceTypeListener testTypeListener = new TestServiceTypeListener(eventsToExpect);
		discoveryContainer.addServiceTypeListener(testTypeListener);

		synchronized (testTypeListener) {
			// register a service which we expect the test listener to get notified of
			registerService();
			try {
				testTypeListener.wait(waitTimeForProvider);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				fail("Some discovery unrelated threading issues?");
			}
		}
		
		discoveryContainer.removeServiceTypeListener(testTypeListener);
		
		assertNotNull("Test listener didn't receive discovery", testTypeListener.getEvent());
		assertEquals("Test listener received more than expected discovery event", eventsToExpect, testTypeListener.getEvent().length);
		assertTrue("Container mismatch", testTypeListener.getEvent()[eventsToExpect - 1].getLocalContainerID().equals(container.getConnectedID()));
		
		//TODO reregister and verify the listener doesn't receive any events any longer.
	}

}
