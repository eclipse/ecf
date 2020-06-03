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

import org.eclipse.ecf.tests.discovery.DiscoveryTestsWithoutRegister;

public class DnsSdAdvertiserWithoutRegister extends
		DiscoveryTestsWithoutRegister {

	public DnsSdAdvertiserWithoutRegister() {
		super(DnsSdTestHelper.ECF_DISCOVERY_DNSSD + ".advertiser");
	}
}
