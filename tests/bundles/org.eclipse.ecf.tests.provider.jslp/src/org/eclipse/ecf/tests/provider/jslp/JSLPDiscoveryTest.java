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

import org.eclipse.ecf.provider.jslp.container.JSLPDiscoveryContainer;
import org.eclipse.ecf.tests.discovery.DiscoveryTest;

public class JSLPDiscoveryTest extends DiscoveryTest {

	public JSLPDiscoveryTest() {
		super(JSLPDiscoveryContainer.NAME, JSLPDiscoveryContainer.REDISCOVER, new JSLPTestComparator());
	}
	
	public void testJSLPLocatorNull() {
		//Activator.getDefault().getLocator() == null always
		fail("not yet implemented");
	}
	
	public void testJSLPAdvertiserNull() {
		//Activator.getDefault().getAdvertiser() == null always
		fail("not yet implemtend");
	}
	
	public void testJSLPBundleBecomesUnavailable() {
		// dynamic OSGi!
		fail("not yet implemtend");
	}
}
