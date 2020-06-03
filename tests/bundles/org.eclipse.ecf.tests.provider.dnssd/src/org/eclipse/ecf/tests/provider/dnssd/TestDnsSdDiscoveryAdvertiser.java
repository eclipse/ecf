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

import java.util.Collection;

import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.provider.dnssd.DnsSdDiscoveryAdvertiser;
import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

// make non API public for testing
public class TestDnsSdDiscoveryAdvertiser extends DnsSdDiscoveryAdvertiser {
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.dnssd.DnsSdDiscoveryAdvertiser#sendToServer(org.eclipse.ecf.discovery.IServiceInfo, boolean)
	 */
	protected void sendToServer(final IServiceInfo serviceInfo, final boolean mode) {
		super.sendToServer(serviceInfo, mode);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.dnssd.DnsSdDiscoveryAdvertiser#getUpdateDomain(org.xbill.DNS.Name)
	 */
	public Collection getUpdateDomain(final Name zone) throws TextParseException {
		return super.getUpdateDomain(zone);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.dnssd.DnsSdDiscoveryAdvertiser#getAuthoritativeNameServer(org.xbill.DNS.Name)
	 */
	public Collection getAuthoritativeNameServer(final Name zone) throws TextParseException {
		return super.getAuthoritativeNameServer(zone);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.dnssd.DnsSdDiscoveryAdvertiser#getRegistrationDomains(org.eclipse.ecf.discovery.identity.IServiceTypeID)
	 */
	public String[] getRegistrationDomains(IServiceTypeID aServiceTypeId) {
		return super.getRegistrationDomains(aServiceTypeId);
	}

}
