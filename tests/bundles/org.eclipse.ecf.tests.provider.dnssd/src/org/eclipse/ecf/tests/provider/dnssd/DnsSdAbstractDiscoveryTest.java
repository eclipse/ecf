/****************************************************************************
 * Copyright (c) 2010 Markus Alexander Kuppe.
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
package org.eclipse.ecf.tests.provider.dnssd;

import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.eclipse.ecf.discovery.IDiscoveryLocator;
import org.eclipse.ecf.tests.discovery.AbstractDiscoveryTest;

public abstract class DnsSdAbstractDiscoveryTest extends AbstractDiscoveryTest {

	public DnsSdAbstractDiscoveryTest() {
		super(DnsSdTestHelper.ECF_DISCOVERY_DNSSD + ".advertiser");
		setNamingAuthority(DnsSdTestHelper.NAMING_AUTH);
		setScope(DnsSdTestHelper.REG_DOMAIN);
		setServices(new String[]{DnsSdTestHelper.REG_SCHEME});
		setProtocol(DnsSdTestHelper.PROTO);
		setComparator(new DnsSdAdvertiserComparator());
		setTTL(DnsSdTestHelper.TTL);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.AbstractDiscoveryTest#getDiscoveryLocator()
	 */
	protected IDiscoveryAdvertiser getDiscoveryAdvertiser() {
		return Activator.getDefault().getDiscoveryAdvertiser();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.AbstractDiscoveryTest#getDiscoveryLocator()
	 */
	protected IDiscoveryLocator getDiscoveryLocator() {
		return Activator.getDefault().getDiscoveryLocator();
	}
}
