/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.provider.jmdns;

import org.eclipse.ecf.tests.discovery.DiscoveryServiceTest;

public class JMDNSDiscoveryServiceTest extends DiscoveryServiceTest {

	public JMDNSDiscoveryServiceTest() {
		super("ecf.discovery.jmdns");
		setHostname(System.getProperty("net.mdns.interface", "127.0.0.1"));
	}
}
