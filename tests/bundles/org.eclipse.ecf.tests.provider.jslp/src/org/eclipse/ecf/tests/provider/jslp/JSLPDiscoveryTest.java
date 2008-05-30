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

	static {
		// we need SA functionality
		assertFalse("jSLP tests require net.slp.uaonly to be set to false because they need SA functionality.", new Boolean(System.getProperty("net.slp.uaonly")).booleanValue());
		// tests need root privileges to bind to slp port 427 in SA mode
		int port;
		try {
			port = Integer.parseInt(System.getProperty("net.slp.port", "427"));
		} catch (NumberFormatException e) {
			port = 427;
		}
		if(port <= 1024) {
			System.err.println("jSLP tests require root privileges to bind to port 427 (Alternatively the port can be set to a high port via -Dnet.slp.port=theHighPort");
		}
	}

	public JSLPDiscoveryTest() {
		super(JSLPDiscoveryContainer.NAME);
		setWaitTimeForProvider(JSLPDiscoveryContainer.REDISCOVER);
		//TODO-mkuppe https://bugs.eclipse.org/bugs/show_bug.cgi?id=230182
		setComparator(new JSLPTestComparator());
		//TODO-mkuppe https://bugs.eclipse.org/bugs/show_bug.cgi?id=218308
		setScope("default");
	}
	
	public void testJSLPBundleBecomesUnavailable() {
		// dynamic OSGi!
		fail("not yet implemtend");
	}
}
