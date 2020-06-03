/****************************************************************************
 * Copyright (c) 2009, 2010 Markus Alexander Kuppe.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Markus Alexander Kuppe (ecf-dev_eclipse.org <at> lemmster <dot> de) - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.provider.discovery;

import org.eclipse.ecf.core.util.StringUtils;

public class WithoutJMDNSCompositeDiscoveryServiceContainerTest extends
	SingleCompositeDiscoveryServiceContainerTest {

	public WithoutJMDNSCompositeDiscoveryServiceContainerTest() {
		super("ecf.discovery.jmdns", "org.eclipse.ecf.provider.jslp.container.JSLPDiscoveryContainer");
		String[] ips;
		// tests need root privileges to bind to slp port 427 in SA mode
		try {
			String str = System.getProperty("net.slp.interfaces", "127.0.0.1");
			ips = StringUtils.split(str, ",");
		} catch (Exception e) {
			ips = new String[]{"127.0.0.1"};
		}
		setHostname(ips[0]);
	}
}
