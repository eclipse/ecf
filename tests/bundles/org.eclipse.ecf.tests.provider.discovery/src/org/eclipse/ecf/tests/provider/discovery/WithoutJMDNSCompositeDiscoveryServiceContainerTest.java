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

public class WithoutJMDNSCompositeDiscoveryServiceContainerTest extends
	SingleCompositeDiscoveryServiceContainerTest {

	public WithoutJMDNSCompositeDiscoveryServiceContainerTest() {
		super("org.eclipse.ecf.provider.jmdns");
		String[] ips;
		// tests need root privileges to bind to slp port 427 in SA mode
		try {
			String str = System.getProperty("net.slp.interfaces", "127.0.0.1");
			ips = str.split(",");
		} catch (Exception e) {
			ips = new String[]{"127.0.0.1"};
		}
		setHostname(ips[0]);
	}
}
