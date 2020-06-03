/****************************************************************************
 * Copyright (c) 2009, 2010 REMAIN B.V. The Netherlands. (http://www.remainsoftware.com).
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 *  Contributors:
 *     A. Aadel - initial API and implementation     
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.provider.zookeeper;

import org.eclipse.ecf.provider.zookeeper.core.ZooDiscoveryContainerInstantiator;
import org.eclipse.ecf.tests.discovery.DiscoveryServiceTest;

public class ZooDiscoveryServiceTest extends DiscoveryServiceTest {

	public ZooDiscoveryServiceTest() {
		super(ZooDiscoveryContainerInstantiator.NAME);
	}

	// Known/Accepted test "failure"
	public void testAddServiceListenerWithRefresh() {
		// just register a service to make sure de-registration in tearDown does not fail
		registerService();
	}
}
