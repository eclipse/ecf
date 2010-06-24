/*******************************************************************************
 * Copyright (c) 2010 Markus Alexander Kuppe.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Alexander Kuppe (ecf-dev_eclipse.org <at> lemmster <dot> de) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.tests.provider.dnssd;

import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.eclipse.ecf.discovery.IDiscoveryLocator;
import org.eclipse.ecf.tests.discovery.AbstractDiscoveryTest;
import org.eclipse.ecf.tests.discovery.Activator;

public class DnsSdAdvertiserServiceTest extends AbstractDiscoveryTest {

	public DnsSdAdvertiserServiceTest() {
		super(DnsSdTestHelper.ECF_DISCOVERY_DNSSD);
		setNamingAuthority(DnsSdTestHelper.NAMING_AUTH);
		setScope(DnsSdTestHelper.DOMAIN);
		setServices(new String[]{DnsSdTestHelper.SCHEME});
		setProtocol(DnsSdTestHelper.PROTO);
		setComparator(new DnsSdDiscoveryComparator());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.AbstractDiscoveryTest#getDiscoveryLocator()
	 */
	protected IDiscoveryAdvertiser getDiscoveryAdvertiser() {
		return Activator.getDefault().getDiscoveryAdvertiser(containerUnderTest);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.AbstractDiscoveryTest#getDiscoveryLocator()
	 */
	protected IDiscoveryLocator getDiscoveryLocator() {
		return Activator.getDefault().getDiscoveryLocator(containerUnderTest);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.provider.dnssd.DnsSdDiscoveryServiceTest#testRegisterService()
	 */
	public void testRegisterService() throws ContainerConnectException {
		fail("Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.provider.dnssd.DnsSdDiscoveryServiceTest#testUnregisterService()
	 */
	public void testUnregisterService() throws ContainerConnectException {
		fail("Not yet implemented");
	}
}
