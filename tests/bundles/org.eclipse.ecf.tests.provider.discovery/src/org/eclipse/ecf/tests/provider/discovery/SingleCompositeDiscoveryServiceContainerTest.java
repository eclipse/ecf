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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.eclipse.ecf.tests.discovery.Activator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public abstract class SingleCompositeDiscoveryServiceContainerTest extends
		CompositeDiscoveryServiceContainerTest {

	private static int testMethods;
	private static int testMethodsLeft;
	
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

	private String bundleName;

	public SingleCompositeDiscoveryServiceContainerTest(String aBundleName) {
		bundleName = aBundleName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.provider.discovery.CompositeDiscoveryServiceContainerTest#setUp()
	 */
	protected void setUp() throws Exception {
		if(testMethodsLeft == testMethods) {
			Bundle bundle = null;
			// stop the bundle assuming there is only one installed
			BundleContext context = Activator.getDefault().getContext();
			Bundle[] bundles = context.getBundles();
			for(int i = 0; i < bundles.length; i++) {
				Bundle aBundle = bundles[i];
				if(aBundle.getSymbolicName().equals(bundleName)) {
					bundle = aBundle;
					break;
				}
			}
			assertNotNull(bundleName + " bundle not found", bundle);
			assertTrue(bundle.getState() == Bundle.ACTIVE);
			bundle.stop();
			assertTrue(bundle.getState() == Bundle.RESOLVED);
		}
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.DiscoveryTest#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		if(--testMethodsLeft == 0) {
			Bundle bundle = null;
			// stop the bundle assuming there is only one installed
			BundleContext context = Activator.getDefault().getContext();
			Bundle[] bundles = context.getBundles();
			for(int i = 0; i < bundles.length; i++) {
				Bundle aBundle = bundles[i];
				if(aBundle.getSymbolicName().equals(bundleName)) {
					bundle = aBundle;
					break;
				}
			}
			assertNotNull(bundleName + " bundle not found", bundle);
			assertTrue(bundle.getState() == Bundle.RESOLVED);
			bundle.start();
			assertTrue(bundle.getState() == Bundle.ACTIVE);
			
			// reset so other instances can reuse
			testMethodsLeft = testMethods;
		}
	}
}
