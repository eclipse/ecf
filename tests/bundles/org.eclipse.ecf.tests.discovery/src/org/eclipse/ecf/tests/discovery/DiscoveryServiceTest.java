/****************************************************************************
* Copyright (c) 2004 Composent, Inc. and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Composent, Inc. - initial API and implementation
*****************************************************************************/

package org.eclipse.ecf.tests.discovery;

import java.util.Arrays;
import java.util.Properties;

import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.events.IContainerEvent;
import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.eclipse.ecf.discovery.IDiscoveryLocator;
import org.eclipse.ecf.discovery.IServiceEvent;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.IServiceListener;
import org.eclipse.ecf.discovery.IServiceTypeListener;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.tests.discovery.listener.TestServiceListener;
import org.eclipse.ecf.tests.discovery.listener.TestServiceTypeListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public abstract class DiscoveryServiceTest extends DiscoveryTest {

	public DiscoveryServiceTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.AbstractDiscoveryTest#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		discoveryLocator.purgeCache();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.AbstractDiscoveryTest#getDiscoveryLocator()
	 */
	protected IDiscoveryLocator getDiscoveryLocator() {
		return Activator.getDefault().getDiscoveryLocator(containerUnderTest);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.AbstractDiscoveryTest#getDiscoveryLocator()
	 */
	protected IDiscoveryAdvertiser getDiscoveryAdvertiser() {
		return Activator.getDefault().getDiscoveryAdvertiser(containerUnderTest);
	}
	
	// Check newly added IServiceListener is notified about service discovered _before_ the listener is registered
//	public void testGetPreregisteredService() {
//		IServiceInfo[] services = discoveryLocator.getServices();
//		assertTrue("No Services must be registerd at this point " + (services.length == 0 ? "" : services[0].toString()), services.length == 0);
//
//		registerService();
//		
//		final TestServiceListener tsl = new TestServiceListener(eventsToExpect, discoveryLocator);
//		Properties props = new Properties();
//		props.put(IDiscoveryLocator.CONTAINER_NAME, containerUnderTest);
//		props.put(IServiceListener.Cache.USE, Boolean.TRUE);
//		BundleContext ctxt = Activator.getDefault().getContext();
//		ServiceRegistration registration = ctxt.registerService(IServiceListener.class.getName(), tsl, props);
//		
//		// No need to wait() on TSL here
//		
//		registration.unregister();
//		
//		IContainerEvent[] event = tsl.getEvent();
//		assertNotNull("Test listener didn't receive any discovery event", event);
//		assertEquals("Test listener received unexpected amount of discovery events: \n\t" + Arrays.asList(event), eventsToExpect, event.length);
//		IServiceInfo serviceInfo2 = ((IServiceEvent) event[eventsToExpect - 1]).getServiceInfo();
//		assertTrue("IServiceInfo should match, expected:\n\t" + serviceInfo + " but was \n\t" + serviceInfo2, comparator.compare(serviceInfo2, serviceInfo) == 0);
//	}
	
	public void testGetRefreshService() {
		IServiceInfo[] services = discoveryLocator.getServices();
		assertTrue("No Services must be registerd at this point " + (services.length == 0 ? "" : services[0].toString()), services.length == 0);

		registerService();
		services = discoveryLocator.getServices();
		assertTrue("A single services must be registerd at this point " + (services.length != 1 ? "" : services.toString()), services.length == 1);
		try {
			// Purge the DiscoveryServiceListener explicitly
			//discoveryLocator.purgeCache();
			
			final ThreadTestServiceListener tsl = new ThreadTestServiceListener(eventsToExpect, discoveryLocator);
			Properties props = new Properties();
			props.put(IDiscoveryLocator.CONTAINER_NAME, containerUnderTest);
			props.put(IServiceListener.Cache.REFRESH, Boolean.TRUE);
			//props.put(IServiceListener.Cache.USE, Boolean.TRUE);
			BundleContext ctxt = Activator.getDefault().getContext();
			ServiceRegistration registration = ctxt.registerService(IServiceListener.class.getName(), tsl, props);
			
			// Because the cache has been purged, it shouldn't know about the previously registered service
			IContainerEvent[] event = tsl.getEvent();
			assertEquals("Test listener received unexpected amount of discovery events: \n\t" + Arrays.asList(event), 0, event.length);
			
			// Here's is a race condition between the DiscoveryServiceListener
			// already actively discovering services and us. If the discovery of
			// the service has finished before we wait on the tsl, we will miss
			// the event? 
			// OTOH won't the event be received by the tsl anyway and
			// we just have to wait for the timeout?
			
			// IServiceListener.Cache.REFRESH should have triggered re-discovery 
			synchronized (tsl) {
				// register a service which we expect the test listener to get notified of
				try {
					tsl.wait(waitTimeForProvider);
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
					fail("Some discovery unrelated threading issues?");
				}
			}
			
			registration.unregister();
			
			event = tsl.getEvent();
			assertTrue("Discovery event must have originated in backend thread", Thread.currentThread() != tsl.getCallingThread());
			assertNotNull("Test listener didn't receive any discovery event", event);
			assertEquals("Test listener received unexpected amount of discovery events: \n\t" + Arrays.asList(event), eventsToExpect, event.length);
			IServiceInfo serviceInfo2 = ((IServiceEvent) event[eventsToExpect - 1]).getServiceInfo();
			assertTrue("IServiceInfo should match, expected:\n\t" + serviceInfo + " but was \n\t" + serviceInfo2, comparator.compare(serviceInfo2, serviceInfo) == 0);
		} finally {
			//de-register the manually registered service manually again.
			// 1. registerService(..)
			// 2. addListenerRegisterAndWait(..)
			unregisterService();
		}
	}

	public void testAddServiceListenerIServiceListenerOSGi() throws ContainerConnectException {
		IServiceInfo[] services = discoveryLocator.getServices();
		assertTrue("No Services must be registerd at this point " + (services.length == 0 ? "" : services[0].toString()), services.length == 0);

		final TestServiceListener tsl = new TestServiceListener(eventsToExpect, discoveryLocator);

		Properties props = new Properties();
		props.put(IDiscoveryLocator.CONTAINER_NAME, containerUnderTest);
		BundleContext ctxt = Activator.getDefault().getContext();
		ServiceRegistration registration = ctxt.registerService(IServiceListener.class.getName(), tsl, props);
		
		addListenerRegisterAndWait(tsl, serviceInfo);

		registration.unregister();
		
		IContainerEvent[] event = tsl.getEvent();
		assertNotNull("Test listener didn't receive any discovery event", event);
		assertEquals("Test listener received unexpected amount of discovery events: \n\t" + Arrays.asList(event), eventsToExpect, event.length);
		IServiceInfo serviceInfo2 = ((IServiceEvent) event[eventsToExpect - 1]).getServiceInfo();
		assertTrue("IServiceInfo should match, expected:\n\t" + serviceInfo + " but was \n\t" + serviceInfo2, comparator.compare(serviceInfo2, serviceInfo) == 0);
	}
	
	public void testAddServiceListenerIServiceTypeIDIServiceListenerOSGi() throws ContainerConnectException {
		IServiceInfo[] services = discoveryLocator.getServices();
		assertTrue("No Services must be registerd at this point " + (services.length == 0 ? "" : services[0].toString()), services.length == 0);

		final TestServiceListener tsl = new TestServiceListener(eventsToExpect, discoveryLocator);

		IServiceTypeID serviceTypeID = serviceInfo.getServiceID().getServiceTypeID();
		Properties props = new Properties();
		props.put("org.eclipse.ecf.discovery.services", serviceTypeID.getServices());
		props.put("org.eclipse.ecf.discovery.scopes", serviceTypeID.getScopes());
		props.put("org.eclipse.ecf.discovery.protocols", serviceTypeID.getProtocols());
		props.put("org.eclipse.ecf.discovery.namingauthority", serviceTypeID.getNamingAuthority());
		props.put(IDiscoveryLocator.CONTAINER_NAME, containerUnderTest);

		BundleContext ctxt = Activator.getDefault().getContext();
		ServiceRegistration registration = ctxt.registerService(IServiceListener.class.getName(), tsl, props);

		addListenerRegisterAndWait(tsl, serviceInfo);

		registration.unregister();
		
		IContainerEvent[] event = tsl.getEvent();
		assertNotNull("Test listener didn't receive discovery", event);
		assertEquals("Test listener received unexpected amount of discovery events: \n\t" + Arrays.asList(event), eventsToExpect, event.length);
		IServiceInfo serviceInfo2 = ((IServiceEvent) event[eventsToExpect - 1]).getServiceInfo();
		assertTrue("IServiceInfo should match, expected:\n\t" + serviceInfo + " but:\n\t" + serviceInfo2, comparator.compare(serviceInfo2, serviceInfo) == 0);
	}

	public void testAddServiceListenerIServiceTypeIDIServiceListenerOSGiWildcards() throws ContainerConnectException {
		IServiceInfo[] services = discoveryLocator.getServices();
		assertTrue("No Services must be registerd at this point " + (services.length == 0 ? "" : services[0].toString()), services.length == 0);

		final TestServiceListener tsl = new TestServiceListener(eventsToExpect, discoveryLocator);

		Properties props = new Properties();
		props.put("org.eclipse.ecf.discovery.services", "*");
		props.put("org.eclipse.ecf.discovery.scopes", "*");
		props.put("org.eclipse.ecf.discovery.protocols", "*");
		props.put("org.eclipse.ecf.discovery.namingauthority", "*");
		props.put(IDiscoveryLocator.CONTAINER_NAME, containerUnderTest);

		BundleContext ctxt = Activator.getDefault().getContext();
		ServiceRegistration registration = ctxt.registerService(IServiceListener.class.getName(), tsl, props);

		addListenerRegisterAndWait(tsl, serviceInfo);

		registration.unregister();
		
		IContainerEvent[] event = tsl.getEvent();
		assertNotNull("Test listener didn't receive discovery", event);
		assertEquals("Test listener received unexpected amount of discovery events: \n\t" + Arrays.asList(event), eventsToExpect, event.length);
		IServiceInfo serviceInfo2 = ((IServiceEvent) event[eventsToExpect - 1]).getServiceInfo();
		assertTrue("IServiceInfo should match, expected:\n\t" + serviceInfo + " but:\n\t" + serviceInfo2, comparator.compare(serviceInfo2, serviceInfo) == 0);
	}
	
	public void testAddServiceTypeListenerOSGi() throws ContainerConnectException {
		IServiceInfo[] services = discoveryLocator.getServices();
		assertTrue("No Services must be registerd at this point " + (services.length == 0 ? "" : services[0].toString()), services.length == 0);

		final TestServiceTypeListener testTypeListener = new TestServiceTypeListener(eventsToExpect);
		Properties props = new Properties();
		props.put(IDiscoveryLocator.CONTAINER_NAME, containerUnderTest);
		BundleContext ctxt = Activator.getDefault().getContext();
		ServiceRegistration registration = ctxt.registerService(IServiceTypeListener.class.getName(), testTypeListener, props);

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
		
		registration.unregister();
		
		IContainerEvent[] event = testTypeListener.getEvent();
		assertNotNull("Test listener didn't receive discovery", event);
		assertEquals("Test listener received unexpected amount of discovery events: \n\t" + Arrays.asList(event), eventsToExpect, event.length);
	}
}
