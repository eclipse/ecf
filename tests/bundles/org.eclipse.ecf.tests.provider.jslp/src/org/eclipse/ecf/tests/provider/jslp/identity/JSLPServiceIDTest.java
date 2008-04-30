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

import java.util.Arrays;

import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.provider.jslp.identity.JSLPNamespace;
import org.eclipse.ecf.tests.discovery.identity.ServiceIDTest;

public class JSLPServiceIDTest extends ServiceIDTest {

	private static final String IANA = "iana";

	public JSLPServiceIDTest() {
		super(JSLPNamespace.NAME);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.identity.ServiceIDTest#testCreateServiceTypeIDFromInternalString()
	 */
	public void testCreateServiceTypeIDFromInternalString() {
		final String internalRep = "service:foo.eclipse:bar";
		final IServiceID sid = (IServiceID) createIDFromString(internalRep);
		final IServiceTypeID stid = sid.getServiceTypeID();

		assertEquals(internalRep, stid.getInternal());
		assertNotSame(internalRep, stid.getName());
		
		assertEquals("eclipse", stid.getNamingAuthority());
		assertTrue(Arrays.equals(new String[] {"service", "foo", "bar"}, stid.getServices()));
		assertTrue(Arrays.equals(new String[] {"default"}, stid.getScopes()));
		assertTrue(Arrays.equals(new String[] {"unknown"}, stid.getProtocols()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.identity.ServiceIDTest#testCreateServiceTypeIDFromInternalString()
	 */
	public void testCreateServiceTypeIDFromInternalStringWithDefaultNamingAuthority() {
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
