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
package org.eclipse.ecf.tests.discovery.identity;

import java.util.Arrays;

import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.discovery.identity.ServiceIDFactory;
import org.eclipse.ecf.tests.discovery.AbstractDiscoveryTest;

public abstract class ServiceIDTest extends AbstractDiscoveryTest {

	protected String namespace;

	public ServiceIDTest(String string) {
		namespace = string;
	}

	protected IServiceID createIDFromString(String serviceType) {
		try {
			return createIDFromStringWithEx(serviceType);
		} catch (final IDCreateException e) {
			fail(e.getMessage());
		} catch (final ClassCastException e) {
			fail(e.getMessage());
		}
		return null;
	}

	protected IServiceID createIDFromStringWithEx(String serviceType) throws IDCreateException {
		return ServiceIDFactory.getDefault().createServiceID(IDFactory.getDefault().getNamespaceByName(namespace), serviceType, null);
	}

	protected IServiceID createIDFromServiceTypeID(IServiceTypeID serviceType) {
		try {
			return createIDFromServiceTypeIDWithEx(serviceType);
		} catch (final IDCreateException e) {
			fail(e.getMessage());
		} catch (final ClassCastException e) {
			fail(e.getMessage());
		}
		return null;
	}

	protected IServiceID createIDFromServiceTypeIDWithEx(IServiceTypeID serviceType) throws IDCreateException {
		return ServiceIDFactory.getDefault().createServiceID(IDFactory.getDefault().getNamespaceByName(namespace), serviceType, null);
	}

	public void testServiceTypeIDWithNullString() {
		try {
			createIDFromStringWithEx(null);
		} catch (final IDCreateException ex) {
			return;
		}
		fail();
	}

	public void testServiceTypeIDWithEmptyString() {
		try {
			createIDFromStringWithEx("");
		} catch (final IDCreateException ex) {
			return;
		}
		fail();
	}

	/*
	 * use case: consumer instantiates a IServiceTypeID with the generic (ECF) String
	 */
	public void testServiceTypeIDWithECFGenericString() {
		final IServiceID sid = createIDFromString(SERVICE_TYPE);
		final IServiceTypeID stid = sid.getServiceTypeID();
		assertEquals(stid.getName(), SERVICE_TYPE);
		assertEquals(stid.getNamingAuthority(), NAMINGAUTHORITY);
		assertTrue(Arrays.equals(stid.getProtocols(), new String[] {PROTOCOL}));
		assertTrue(Arrays.equals(stid.getScopes(), new String[] {SCOPE}));
		assertTrue(Arrays.equals(stid.getServices(), SERVICES));
	}

	/*
	 * use case: consumer instantiates a IServiceTypeID with the generic (ECF) String
	 */
	public void testServiceTypeIDWithECFGenericString2() {
		final String serviceType = "_service._dns-srv._udp.ecf.eclipse.org._IANA";
		final IServiceID sid = createIDFromString(serviceType);
		final IServiceTypeID stid = sid.getServiceTypeID();
		assertEquals(stid.getName(), serviceType);
		assertEquals(stid.getNamingAuthority(), "IANA");
		assertTrue(Arrays.equals(stid.getProtocols(), new String[] {"udp"}));
		assertTrue(Arrays.equals(stid.getScopes(), new String[] {"ecf.eclipse.org"}));
		assertTrue(Arrays.equals(stid.getServices(), new String[] {"service", "dns-srv"}));
	}

	/*
	 * use case: consumer instantiates a IServiceTypeID with the generic (ECF) String
	 */
	public void testServiceTypeIDWithECFGenericString3() {
		final String serviceType = "_service._dns-srv._udp.ecf.eclipse.org._ECLIPSE";
		final IServiceID sid = createIDFromString(serviceType);
		final IServiceTypeID stid = sid.getServiceTypeID();
		assertEquals(stid.getName(), serviceType);
		assertEquals(stid.getNamingAuthority(), "ECLIPSE");
		assertTrue(Arrays.equals(stid.getProtocols(), new String[] {"udp"}));
		assertTrue(Arrays.equals(stid.getScopes(), new String[] {"ecf.eclipse.org"}));
		assertTrue(Arrays.equals(stid.getServices(), new String[] {"service", "dns-srv"}));
	}

	/*
	 * use case: conversion from one IServiceTypeID to another (provider A -> provider B)
	 */
	public void testServiceTypeIDWithServiceTypeID() throws IDCreateException {
		final Namespace ns = IDFactory.getDefault().getNamespaceByName(namespace);
		final IServiceTypeID aServiceTypeID = ServiceIDFactory.getDefault().createServiceID(ns, "_service._ecf._foo._bar._tcp.ecf.eclipse.org._IANA").getServiceTypeID();
		final IServiceID sid = createIDFromServiceTypeID(aServiceTypeID);
		final IServiceTypeID stid = sid.getServiceTypeID();

		// this is the only differences
		assertNotSame(aServiceTypeID.getInternal(), stid.getInternal());

		// members should be the same
		assertEquals(aServiceTypeID.getNamingAuthority(), stid.getNamingAuthority());
		assertTrue(Arrays.equals(aServiceTypeID.getServices(), stid.getServices()));
		assertTrue(Arrays.equals(aServiceTypeID.getScopes(), stid.getScopes()));
		assertTrue(Arrays.equals(aServiceTypeID.getProtocols(), stid.getProtocols()));

		// logically they should be the same
		assertTrue(aServiceTypeID.hashCode() == stid.hashCode());
		assertEquals(aServiceTypeID, stid);
		assertEquals(stid, aServiceTypeID);

		// should be possible to create a new instance from the string representation of the other
		createFromAnother(aServiceTypeID, stid);
		createFromAnother(stid, aServiceTypeID);
	}

	/**
	 * Creates a new instance of IServiceTypeId with the Namespace of the second parameter and the instance of the first parameter 
	 * @param aServiceTypeID Used as a prototype
	 * @param stid Namespace to use
	 */
	private void createFromAnother(IServiceTypeID aServiceTypeID, IServiceTypeID stid) {
		final String name = aServiceTypeID.getName();
		final Namespace namespace2 = stid.getNamespace();
		IServiceID sid = null;
		try {
			sid = (IServiceID) namespace2.createInstance(new Object[] {name, null});
		} catch (final IDCreateException e) {
			fail("it should have been possible to create a new instance of " + stid.getClass().getName() + " from the string rep of " + aServiceTypeID.getClass().getName());
		}
		IServiceTypeID instance = sid.getServiceTypeID();
		assertTrue(instance.hashCode() == stid.hashCode());
		//TODO-mkuppe decide if equality should be handled by the namespace for IServiceTypeIDs?
		assertEquals(instance, stid);
		assertEquals(stid, instance);
		assertTrue(instance.hashCode() == aServiceTypeID.hashCode());
		assertEquals(instance, aServiceTypeID);
		assertEquals(aServiceTypeID, instance);
	}

	/*
	 * use case: creates the IServiceTypeID from the internal representation of the discovery provider
	 * to be implemented by subclasses
	 */
	public abstract void testCreateServiceTypeIDWithProviderSpecificString();
}
