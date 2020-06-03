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


public class WithoutJSLPCompositeDiscoveryServiceContainerTest extends
	SingleCompositeDiscoveryServiceContainerTest {

	public WithoutJSLPCompositeDiscoveryServiceContainerTest() {
		super("ecf.discovery.jslp", "org.eclipse.ecf.provider.jmdns.container.JMDNSDiscoveryContainer");
		setHostname(System.getProperty("net.mdns.interface", "127.0.0.1"));
	}
}
