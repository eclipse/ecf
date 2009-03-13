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
package org.eclipse.ecf.tests.discovery.all;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ecf.tests.osgi.services.discovery.PublishTest;
import org.eclipse.ecf.tests.osgi.services.discovery.local.DistributedOSGiBasedStaticInformationTest;
import org.eclipse.ecf.tests.provider.discovery.CompositeDiscoveryContainerTest;
import org.eclipse.ecf.tests.provider.discovery.CompositeDiscoveryContainerWithoutRegTest;
import org.eclipse.ecf.tests.provider.discovery.CompositeDiscoveryServiceContainerTest;
import org.eclipse.ecf.tests.provider.discovery.WithoutJMDNSCompositeDiscoveryServiceContainerTest;
import org.eclipse.ecf.tests.provider.discovery.WithoutJSLPCompositeDiscoveryServiceContainerTest;
import org.eclipse.ecf.tests.provider.jmdns.JMDNSDiscoveryServiceTest;
import org.eclipse.ecf.tests.provider.jmdns.JMDNSDiscoveryTest;
import org.eclipse.ecf.tests.provider.jmdns.JMDNSDiscoveryWithoutRegTest;
import org.eclipse.ecf.tests.provider.jmdns.JMDNSServiceInfoTest;
import org.eclipse.ecf.tests.provider.jmdns.identity.JMDNSServiceIDTest;
import org.eclipse.ecf.tests.provider.jslp.JSLPDiscoveryServiceTest;
import org.eclipse.ecf.tests.provider.jslp.JSLPDiscoveryTest;
import org.eclipse.ecf.tests.provider.jslp.JSLPDiscoveryWithoutRegTest;
import org.eclipse.ecf.tests.provider.jslp.JSLPServiceInfoTest;
import org.eclipse.ecf.tests.provider.jslp.identity.JSLPServiceIDTest;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.ecf.tests.discovery.all");
		//$JUnit-BEGIN$
		
		String str = System.getProperty("org.eclipse.ecf.tests.discovery.all", "1");
		int iterations = Integer.parseInt(str);
		
		for (int i = 0; i < iterations; i++) {
			// ECF RFC 119 discovery
			suite.addTestSuite(PublishTest.class);
			
			// SEN file based discovery
			suite.addTestSuite(DistributedOSGiBasedStaticInformationTest.class);
			
			// jSLP
			suite.addTestSuite(JSLPDiscoveryServiceTest.class);
			suite.addTestSuite(JSLPDiscoveryTest.class);
			suite.addTestSuite(JSLPDiscoveryWithoutRegTest.class);
			suite.addTestSuite(JSLPServiceInfoTest.class);
			suite.addTestSuite(JSLPServiceIDTest.class);
			
			// JmDNS
			suite.addTestSuite(JMDNSDiscoveryServiceTest.class);
			suite.addTestSuite(JMDNSDiscoveryTest.class);
			suite.addTestSuite(JMDNSDiscoveryWithoutRegTest.class);
			suite.addTestSuite(JMDNSServiceInfoTest.class);
			suite.addTestSuite(JMDNSServiceIDTest.class);

			// composite
			suite.addTestSuite(CompositeDiscoveryServiceContainerTest.class);
			suite.addTestSuite(CompositeDiscoveryContainerTest.class);
			suite.addTestSuite(CompositeDiscoveryContainerWithoutRegTest.class);
			suite.addTestSuite(WithoutJSLPCompositeDiscoveryServiceContainerTest.class);
			suite.addTestSuite(WithoutJMDNSCompositeDiscoveryServiceContainerTest.class);
		}
		//$JUnit-END$
		return suite;
	}

}
