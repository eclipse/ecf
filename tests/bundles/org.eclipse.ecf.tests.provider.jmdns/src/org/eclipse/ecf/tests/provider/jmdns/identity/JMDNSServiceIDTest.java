/****************************************************************************
 * Copyright (c) 2007 Versant Corp.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.tests.provider.jmdns.identity;

import org.eclipse.ecf.tests.discovery.identity.ServiceIDTest;

/**
 *
 */
public class JMDNSServiceIDTest extends ServiceIDTest {
//	private static final String[] scopes = {"local", "default"};
//	private static final String[] services = {"ecf", "foo", "bar"};
//	private static final String[] protocols = {"tcp", "udp"};

	public JMDNSServiceIDTest() {
		super("ecf.namespace.jmdns");//, services, scopes, protocols, "ecf-eclipse");
	}

//	public void testJMDNSServiceTypeIDWithIPv6() {
//		final String serviceType = "1.0.0.0.0.c.e.f.f.f.6.5.0.5.2.0.0.0.0.0.0.0.0.0.0.0.0.0.0.8.e.f.ip6.arpa.";
//		fail("Not implemented yet, don't know how to handle this service type " + serviceType + " if it even is one");
//	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.identity.ServiceIDTest#testCreateServiceTypeIDFromInternalString()
	 */
	public void testCreateServiceTypeIDWithProviderSpecificString() {
		final String internalRep = "_service._tcp.local";
		createIDFromString(internalRep);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.identity.ServiceIDTest#testCreateServiceTypeIDFromInternalString()
	 */
	public void testCreateServiceTypeIDFromInternalString2() {
		final String internalRep = "_service._tcp.ecf.eclipse.org";
		createIDFromString(internalRep);
	}
}
