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
package org.eclipse.ecf.tests.provider.jslp;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ecf.tests.provider.jslp.identity.JSLPServiceIDTest;


public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.ecf.tests.discovery.jslp");
		//$JUnit-BEGIN$
		suite.addTestSuite(JSLPServiceInfoTest.class);
		suite.addTestSuite(JSLPServiceIDTest.class);
		suite.addTestSuite(JSLPDiscoveryTest.class);
		suite.addTestSuite(JSLPDiscoveryServiceTest.class);
		//$JUnit-END$
		return suite;
	}

}
