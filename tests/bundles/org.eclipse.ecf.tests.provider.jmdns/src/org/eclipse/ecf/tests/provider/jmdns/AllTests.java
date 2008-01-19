/*******************************************************************************
 * Copyright (c) 2007 Versant Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.tests.provider.jmdns;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ecf.tests.provider.jmdns.identity.JMDNSServiceIDTest;
import org.eclipse.ecf.tests.provider.jmdns.remoteservice.JMDNSRemoteServiceDiscoveryTest;

public class AllTests {

	public static Test suite() {
		final TestSuite suite = new TestSuite("Test for org.eclipse.ecf.tests.discovery.jmdns");
		//$JUnit-BEGIN$
		suite.addTestSuite(JMDNSServiceIDTest.class);
		suite.addTestSuite(JMDNSDiscoveryServiceTest.class);
		suite.addTestSuite(JMDNSRemoteServiceDiscoveryTest.class);
		//$JUnit-END$
		return suite;
	}

}
