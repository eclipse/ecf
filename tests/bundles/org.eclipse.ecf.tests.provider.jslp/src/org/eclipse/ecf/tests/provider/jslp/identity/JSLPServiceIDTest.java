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
package org.eclipse.ecf.tests.provider.jslp.identity;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.internal.provider.jslp.ServicePropertiesAdapter;
import org.eclipse.ecf.internal.provider.jslp.ServiceURLAdapter;
import org.eclipse.ecf.provider.jslp.container.JSLPServiceInfo;
import org.eclipse.ecf.provider.jslp.identity.JSLPNamespace;
import org.eclipse.ecf.provider.jslp.identity.JSLPServiceTypeID;
import org.eclipse.ecf.tests.discovery.identity.ServiceIDTest;

import ch.ethz.iks.slp.ServiceLocationException;
import ch.ethz.iks.slp.ServiceURL;

public class JSLPServiceIDTest extends ServiceIDTest {

	private static final String IANA = "iana";

	public JSLPServiceIDTest() {
		super(JSLPNamespace.NAME);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.identity.ServiceIDTest#testCreateServiceTypeIDFromInternalString()
	 */
	public void testCreateServiceTypeIDWithProviderSpecificString() {
		final String internalRep = "service:foo.eclipse:bar";
		final IServiceID sid = (IServiceID) createIDFromString(internalRep);
		final IServiceTypeID stid = sid.getServiceTypeID();

		assertEquals(internalRep, stid.getInternal());
		assertTrue(stid.getName().startsWith("_foo._bar"));
		assertTrue(stid.getName().endsWith("._eclipse"));
		
		assertEquals("eclipse", stid.getNamingAuthority());
		assertTrue(Arrays.equals(new String[] {"foo", "bar"}, stid.getServices()));
		assertTrue(Arrays.equals(new String[] {"default"}, stid.getScopes()));
		assertTrue(Arrays.equals(new String[] {"unknown"}, stid.getProtocols()));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.identity.ServiceIDTest#testCreateServiceTypeIDFromInternalString()
	 * 
	 * test from ECF discovery -> jSLP
	 */
	public void testRemoveServicePrefixECFtojSLP() throws ServiceLocationException {
		final IServiceID sid = (IServiceID) createIDFromString(SERVICE_TYPE);
		final JSLPServiceTypeID stid = (JSLPServiceTypeID) sid.getServiceTypeID();

		assertEquals("service:" + SERVICES[0] + ":" + SERVICES[1] + ":" + SERVICES[2], stid.getInternal());
		assertEquals(SERVICE_TYPE, stid.getName());
		
		assertEquals(NAMINGAUTHORITY, stid.getNamingAuthority());
		assertTrue(Arrays.equals(SERVICES, stid.getServices()));
		assertTrue(Arrays.equals(new String[] {SCOPE}, stid.getScopes()));
		assertTrue(Arrays.equals(new String[] {PROTOCOL}, stid.getProtocols()));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.identity.ServiceIDTest#testCreateServiceTypeIDFromInternalString()
	 * 
	 * test from jSLP -> ECF discovery which needs to remove the first occurrence of "service:"
	 */
	public void testCreateByjSLPAndRemoveServicePrefix() throws ServiceLocationException {
		final String internalRep = "service:foo.eclipse:bar";
		final ServiceURL sUrl = new ServiceURL(internalRep + "://localhost:1234/a/path/to/something", ServiceURL.LIFETIME_PERMANENT);

		final IServiceInfo serviceInfo = new JSLPServiceInfo(new ServiceURLAdapter(sUrl), -1, -1, new ServicePropertiesAdapter(new ArrayList()));
		final IServiceID sid = serviceInfo.getServiceID();
		final IServiceTypeID stid = sid.getServiceTypeID();
		
		String internal = stid.getInternal();
		assertEquals(internalRep, internal);
		assertEquals("_foo._bar._unknown.default._eclipse", stid.getName());
		
		assertEquals("eclipse", stid.getNamingAuthority());
		assertTrue(Arrays.equals(new String[] {"foo", "bar"}, stid.getServices()));
		assertTrue(Arrays.equals(new String[] {"default"}, stid.getScopes()));
		assertTrue(Arrays.equals(new String[] {"unknown"}, stid.getProtocols()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.identity.ServiceIDTest#testRemoveServicePrefixWithServiceService()
	 * 
	 * test from jSLP -> ECF discovery which needs to remove the first occurrence of "service:"
	 */
	public void testCreateByjSLPAndRemoveServicePrefixWithServiceService() throws ServiceLocationException {
		final String internalRep = "service:service:foo.eclipse:bar";
		final ServiceURL sUrl = new ServiceURL(internalRep + "://localhost:1234/a/path/to/something", ServiceURL.LIFETIME_PERMANENT);

		final IServiceInfo serviceInfo = new JSLPServiceInfo(new ServiceURLAdapter(sUrl), -1, -1, new ServicePropertiesAdapter(new ArrayList()));
		final IServiceID sid = serviceInfo.getServiceID();
		final IServiceTypeID stid = sid.getServiceTypeID();
		
		assertEquals(internalRep, stid.getInternal());
		assertEquals("_service._foo._bar._unknown.default._eclipse", stid.getName());
		
		assertEquals("eclipse", stid.getNamingAuthority());
		assertTrue(Arrays.equals(new String[] {"service", "foo", "bar"}, stid.getServices()));
		assertTrue(Arrays.equals(new String[] {"default"}, stid.getScopes()));
		assertTrue(Arrays.equals(new String[] {"unknown"}, stid.getProtocols()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.identity.ServiceIDTest#testCreateServiceTypeIDFromInternalString()
	 */
	public void testCreateServiceTypeIDFromSLPStringWithDefaultNamingAuthority() {
		final String internalRep = "service:foo." + IANA + ":bar";
		final IServiceID sid = (IServiceID) createIDFromString(internalRep);
		final IServiceTypeID stid = sid.getServiceTypeID();

		// the internalRep contains "iana" but getInternal may not!
		final int indexOf = stid.getInternal().toLowerCase().indexOf(IANA.toLowerCase());
		assertTrue(indexOf == -1);
		assertEquals(IANA, stid.getNamingAuthority().toLowerCase());
		assertNotSame(internalRep, stid.getName());
	}
}
