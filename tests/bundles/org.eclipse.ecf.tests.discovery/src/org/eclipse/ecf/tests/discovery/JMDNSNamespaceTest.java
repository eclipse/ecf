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

package org.eclipse.ecf.tests.discovery;

import java.util.Arrays;

import junit.framework.TestCase;

import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.discovery.identity.ServiceIDFactory;

/**
 *
 */
public class JMDNSNamespaceTest extends TestCase {
	private IServiceID createID(String serviceType, String serviceName) {
		try {
			return createIDWithEx(serviceType, serviceName);
		} catch (final IDCreateException e) {
			fail(e.getMessage());
		} catch (final ClassCastException e) {
			fail(e.getMessage());
		}
		return null;
	}

	private IServiceID createIDWithEx(String serviceType, String serviceName) throws IDCreateException {
		return ServiceIDFactory.getDefault().createServiceID(IDFactory.getDefault().getNamespaceByName("ecf.namespace.jmdns"), serviceType, serviceName);
	}

	public void testJMDNSServiceTypeID() {
		final IServiceID sid = createID(DiscoveryServiceTest.TEST_SERVICE_TYPE, null);
		final IServiceTypeID stid = sid.getServiceTypeID();
		assertEquals(stid.getName(), DiscoveryServiceTest.TEST_SERVICE_TYPE);
		assertEquals(stid.getNamingAuthority(), "IANA");
		assertTrue(Arrays.equals(stid.getProtocols(), new String[] {"tcp"}));
		assertTrue(Arrays.equals(stid.getScopes(), new String[] {"local"}));
		assertTrue(Arrays.equals(stid.getServices(), new String[] {"ecftcp"}));
	}

	public void testJMDNSServiceTypeID2() {
		final String serviceType = "_service._dns-srv._udp.ecf.eclipse.org.";
		final IServiceID sid = createID(serviceType, null);
		final IServiceTypeID stid = sid.getServiceTypeID();
		assertEquals(stid.getName(), serviceType);
		assertEquals(stid.getNamingAuthority(), "IANA");
		assertTrue(Arrays.equals(stid.getProtocols(), new String[] {"udp"}));
		assertTrue(Arrays.equals(stid.getScopes(), new String[] {"ecf.eclipse.org"}));
		assertTrue(Arrays.equals(stid.getServices(), new String[] {"service", "dns-srv"}));
	}

	public void testJMDNSServiceTypeIDWithNullString() {
		try {
			createIDWithEx(null, null);
		} catch (final IDCreateException ex) {
			return;
		}
		fail();
	}

	public void testJMDNSServiceTypeIDWithEmptyString() {
		try {
			createIDWithEx("", null);
		} catch (final IDCreateException ex) {
			return;
		}
		fail();
	}

	/*
	public void testJMDNSServiceTypeIDWithIPv6() {
		final String serviceType = "1.0.0.0.0.c.e.f.f.f.6.5.0.5.2.0.0.0.0.0.0.0.0.0.0.0.0.0.0.8.e.f.ip6.arpa.";
		fail("Not implemented yet, don't know how to handle this service type " + serviceType + " if it even is one");
	}
	*/

}
