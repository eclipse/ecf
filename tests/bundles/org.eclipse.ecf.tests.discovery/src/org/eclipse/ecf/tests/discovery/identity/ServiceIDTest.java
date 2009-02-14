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
import org.eclipse.ecf.discovery.identity.ServiceTypeID;
import org.eclipse.ecf.tests.discovery.AbstractDiscoveryTest;

public abstract class ServiceIDTest extends AbstractDiscoveryTest {

	protected String namespace;
	private String namingAuthority;
	private String[] protocols;
	private String[] scopes;
	private String[] services;

	public ServiceIDTest(String string, String[] services, String[] scopes, String[] protocols, String namingAuthority) {
		namespace = string;
		this.services = services;
		this.scopes = scopes;
		this.protocols = protocols;
		this.namingAuthority = namingAuthority;
	}
	
	public ServiceIDTest(String namespace) {
		this(namespace, SERVICES, new String[]{SCOPE}, new String[]{PROTOCOL}, NAMINGAUTHORITY);
	}

	protected IServiceID createIDFromString(String serviceType) {
		try {
			return createIDFromStringWithEx(serviceType);
		} catch (final ClassCastException e) {
			fail(e.getMessage());
		}
		return null;
	}

	protected IServiceID createIDFromStringWithEx(String serviceType) {
		return ServiceIDFactory.getDefault().createServiceID(IDFactory.getDefault().getNamespaceByName(namespace), serviceType);
	}

	protected IServiceID createIDFromServiceTypeID(IServiceTypeID serviceType) {
		try {
			return createIDFromServiceTypeIDWithEx(serviceType);
		} catch (final ClassCastException e) {
			fail(e.getMessage());
		}
		return null;
	}

	protected IServiceID createIDFromServiceTypeIDWithEx(IServiceTypeID serviceType) {
		return ServiceIDFactory.getDefault().createServiceID(IDFactory.getDefault().getNamespaceByName(namespace), serviceType);
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
		assertTrue(serviceType.equalsIgnoreCase(stid.getName()));
		assertTrue("IANA".equalsIgnoreCase(stid.getNamingAuthority()));
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
	public void testServiceTypeIDWithServiceTypeID() {
		final Namespace ns = IDFactory.getDefault().getNamespaceByName(namespace);
		final IServiceTypeID aServiceTypeID = ServiceIDFactory.getDefault().createServiceID(ns, "_service._ecf._foo._bar._tcp.ecf.eclipse.org._IANA").getServiceTypeID();
		final IServiceID sid = createIDFromServiceTypeID(aServiceTypeID);
		final IServiceTypeID stid = sid.getServiceTypeID();

		// this is the only differences
		assertNotSame(aServiceTypeID.getInternal(), stid.getInternal());

		// members should be the same
		assertTrue(aServiceTypeID.getNamingAuthority().equalsIgnoreCase(stid.getNamingAuthority()));
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
	
	/*
	 * org.eclipse.ecf.discovery.identity.IServiceIDFactory.createServiceID(Namespace, String, String)
	 */
	public void testServiceIDFactory1() {
		String expected = "some Name";
		Namespace namespaceByName = IDFactory.getDefault().getNamespaceByName(namespace);
		IServiceID createServiceID = ServiceIDFactory.getDefault().createServiceID(namespaceByName, "_service._ecf._foo._bar._tcp.ecf.eclipse.org._IANA", expected);
		assertNotNull(createServiceID);
	}

	/*
	 * org.eclipse.ecf.discovery.identity.IServiceIDFactory.createServiceID(Namespace, String, String)
	 */
	public void testServiceIDFactory() {
		String expected = "some Name";
		
		Namespace namespaceByName = IDFactory.getDefault().getNamespaceByName(namespace);
		IServiceID aServiceID = ServiceIDFactory.getDefault().createServiceID(namespaceByName, services, scopes, protocols, namingAuthority, expected);
		assertNotNull(aServiceID);

		IServiceTypeID serviceType = aServiceID.getServiceTypeID();
		assertEquals(namingAuthority, serviceType.getNamingAuthority());
		assertTrue(Arrays.equals(services, serviceType.getServices()));
		assertTrue(Arrays.equals(scopes, serviceType.getScopes()));
		assertTrue(Arrays.equals(protocols, serviceType.getProtocols()));
	}
	
	/*
	 * org.eclipse.ecf.discovery.identity.IServiceIDFactory.createServiceID(Namespace, String[], String[], String[], String, String)
	 */
	public void testServiceIDFactoryNullNA() {
		String expected = "some Name";
		
		try {
			Namespace namespaceByName = IDFactory.getDefault().getNamespaceByName(namespace);
			ServiceIDFactory.getDefault().createServiceID(namespaceByName, services, scopes, protocols, null, expected);
		} catch(IDCreateException e) {
			return;
		}
		fail("Invalid services may cause InvalidIDException");
	}
	
	/*
	 * org.eclipse.ecf.discovery.identity.IServiceIDFactory.createServiceID(Namespace, String[], String[], String[], String, String)
	 */
	public void testServiceIDFactoryNullProto() {
		String expected = "some Name";
		
		try {
			Namespace namespaceByName = IDFactory.getDefault().getNamespaceByName(namespace);
			ServiceIDFactory.getDefault().createServiceID(namespaceByName, services, scopes, null, namingAuthority, expected);
		} catch(IDCreateException e) {
			return;
		}
		fail("Invalid services may cause InvalidIDException");
	}

	/*
	 * org.eclipse.ecf.discovery.identity.IServiceIDFactory.createServiceID(Namespace, String[], String[], String[], String, String)
	 */
	public void testServiceIDFactoryNullServices() {
		String expected = "some Name";
		
		try {
			Namespace namespaceByName = IDFactory.getDefault().getNamespaceByName(namespace);
			ServiceIDFactory.getDefault().createServiceID(namespaceByName, null, scopes, protocols, namingAuthority, expected);
		} catch(IDCreateException e) {
			return;
		}
		fail("Invalid services may cause InvalidIDException");
	}
	
	/*
	 * org.eclipse.ecf.discovery.identity.IServiceIDFactory.createServiceID(Namespace, String[], String[], String[], String, String)
	 */
	public void testServiceIDFactoryNullScope() {
		String expected = "some Name";
		
		try {
			Namespace namespaceByName = IDFactory.getDefault().getNamespaceByName(namespace);
			ServiceIDFactory.getDefault().createServiceID(namespaceByName, services, null, protocols, namingAuthority, expected);
		} catch(IDCreateException e) {
			return;
		}
		fail("Invalid services may cause InvalidIDException");
	}
	
	/*
	 * org.eclipse.ecf.discovery.identity.IServiceIDFactory.createServiceID(Namespace, String[], String)
	 */
	public void testServiceIDFactoryDefaults() {
		String expected = "some Name";
		
		Namespace namespaceByName = IDFactory.getDefault().getNamespaceByName(namespace);
		IServiceID aServiceID = ServiceIDFactory.getDefault().createServiceID(namespaceByName, services, protocols, expected);
		assertNotNull(aServiceID);

		IServiceTypeID serviceType = aServiceID.getServiceTypeID();
		assertTrue(Arrays.equals(services, serviceType.getServices()));
		assertEquals(IServiceTypeID.DEFAULT_NA, serviceType.getNamingAuthority());
		assertTrue(Arrays.equals(IServiceTypeID.DEFAULT_SCOPE, serviceType.getScopes()));
		assertTrue(Arrays.equals(protocols, serviceType.getProtocols()));
	}
	
	/*
	 * org.eclipse.ecf.discovery.identity.IServiceIDFactory.createServiceID(Namespace, String, String)
	 */
	public void testServiceIDFactory2() {
		Namespace namespaceByName = IDFactory.getDefault().getNamespaceByName(namespace);
		ServiceTypeID serviceTypeID = new ServiceTypeID(new TestNamespace(), "_service._ecf._foo._bar._tcp.ecf.eclipse.org._IANA");
		IServiceID createServiceID = ServiceIDFactory.getDefault().createServiceID(namespaceByName, serviceTypeID, "some Name");
		assertNotNull(createServiceID);
		
		// members should be the same
		IServiceTypeID aServiceTypeID = createServiceID.getServiceTypeID();
		assertEquals(aServiceTypeID.getNamingAuthority(), serviceTypeID.getNamingAuthority());
		assertTrue(Arrays.equals(aServiceTypeID.getServices(), serviceTypeID.getServices()));
		assertTrue(Arrays.equals(aServiceTypeID.getScopes(), serviceTypeID.getScopes()));
		assertTrue(Arrays.equals(aServiceTypeID.getProtocols(), serviceTypeID.getProtocols()));
		
		assertSame(namespaceByName, createServiceID.getNamespace());
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
		sid = (IServiceID) namespace2.createInstance(new Object[] {name, null});
		assertNotNull("it should have been possible to create a new instance of ", sid);
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
