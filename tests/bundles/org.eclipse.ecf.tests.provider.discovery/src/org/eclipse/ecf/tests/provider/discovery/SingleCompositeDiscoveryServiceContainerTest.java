/*******************************************************************************
 * Copyright (c) 2009, 2010 Markus Alexander Kuppe.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Alexander Kuppe (ecf-dev_eclipse.org <at> lemmster <dot> de) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.tests.provider.discovery;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.eclipse.ecf.discovery.IDiscoveryLocator;
import org.eclipse.ecf.provider.discovery.CompositeDiscoveryContainer;
import org.eclipse.ecf.tests.discovery.Activator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.service.FindHook;

public abstract class SingleCompositeDiscoveryServiceContainerTest extends
		CompositeDiscoveryServiceContainerTest {

	private static int testMethods;
	private static int testMethodsLeft;
	private static ServiceRegistration findHook;
	
	// count all test methods
	static {
		Method[] methods = SingleCompositeDiscoveryServiceContainerTest.class.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (method.getName().startsWith("test") && method.getModifiers() == Modifier.PUBLIC) {
				testMethods++;
			}
		}
		testMethodsLeft = testMethods;
	}

	private final String ecfDiscoveryContainerName;
	private final String className;

	public SingleCompositeDiscoveryServiceContainerTest(String anECFDiscoveryContainerName, String aClassName) {
		ecfDiscoveryContainerName = anECFDiscoveryContainerName;
		className = aClassName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.provider.discovery.CompositeDiscoveryServiceContainerTest#setUp()
	 */
	protected void setUp() throws Exception {
		if(testMethodsLeft == testMethods) {
			// initially close the existing CDC to get rid of other test left overs
			Activator.getDefault().closeServiceTracker(containerUnderTest);
			
			final BundleContext context = Activator.getDefault().getContext();
			findHook = context.registerService(FindHook.class.getName(), new DiscoveryContainerFilterFindHook(ecfDiscoveryContainerName), null);
		}
		super.setUp();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.DiscoveryTest#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		if(--testMethodsLeft == 0) {
			if(findHook != null) {
				findHook.unregister();
				findHook = null;
			}
			// close tracker to force creation of a new CDC instance
			Activator.getDefault().closeServiceTracker(containerUnderTest);

			// reset so other instances can reuse
			testMethodsLeft = testMethods;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.DiscoveryServiceTest#getDiscoveryLocator()
	 */
	protected IDiscoveryLocator getDiscoveryLocator() {
		final IDiscoveryLocator idl = super.getDiscoveryLocator();
		checkCompositeDiscoveryContainer(className, (CompositeDiscoveryContainer)idl);
		return idl;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.DiscoveryServiceTest#getDiscoveryAdvertiser()
	 */
	protected IDiscoveryAdvertiser getDiscoveryAdvertiser() {
		final IDiscoveryAdvertiser ida = super.getDiscoveryAdvertiser();
		checkCompositeDiscoveryContainer(className, (CompositeDiscoveryContainer)ida);
		return ida;
	}
	
	// make sure the CDC has only a single IDC registered with the correct type
	private static void checkCompositeDiscoveryContainer(final String aClassName, final CompositeDiscoveryContainer cdc) {
		final Collection discoveryContainers = cdc.getDiscoveryContainers();
		assertTrue("Only one IDiscoveryContainer must be registered with the CDC at this point: " + discoveryContainers, discoveryContainers.size() == 1);
		for (final Iterator iterator = discoveryContainers.iterator(); iterator.hasNext();) {
			final IDiscoveryLocator dl = (IDiscoveryLocator) iterator.next();
			assertEquals(aClassName, dl.getClass().getName());
		}
	}

	// Filters the corresponding IDC from the result set that is _not_ supposed to be part of the test 
	private class DiscoveryContainerFilterFindHook implements FindHook {

		private static final String BUNDLE_UNDER_TEST = "org.eclipse.ecf.provider.discovery"; // rename if bundle name change
		private final String containerName;

		public DiscoveryContainerFilterFindHook(String anECFDiscoveryContainerName) {
			containerName = anECFDiscoveryContainerName;
		}

		/* (non-Javadoc)
		 * @see org.osgi.framework.hooks.service.FindHook#find(org.osgi.framework.BundleContext, java.lang.String, java.lang.String, boolean, java.util.Collection)
		 */
		public void find(BundleContext context, String name, String filter, boolean allServices, Collection references) {
			
			// is it the composite discovery bundle who tries to find the service?
			final String symbolicName = context.getBundle().getSymbolicName();
			final Collection removees = new ArrayList();
			if(BUNDLE_UNDER_TEST.equals(symbolicName)) {
				for (final Iterator iterator = references.iterator(); iterator.hasNext();) {
					// filter the corresponding container 
					final ServiceReference serviceReference = (ServiceReference) iterator.next();
					final String property = (String) serviceReference.getProperty(IDiscoveryLocator.CONTAINER_NAME);
					if(property != null && property.equals(containerName)) {
						removees.add(serviceReference);
						System.out.println("Removed reference: " + property);
					}
				}
				references.removeAll(removees );
			}
		}
	}
}
