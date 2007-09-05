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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.ecf.tests.discovery");
		//$JUnit-BEGIN$
		suite.addTestSuite(DiscoveryServiceTest.class);
		suite.addTestSuite(DiscoveryTest.class);
		suite.addTestSuite(JMDNSNamespaceTest.class);
		//$JUnit-END$
		return suite;
	}

}
