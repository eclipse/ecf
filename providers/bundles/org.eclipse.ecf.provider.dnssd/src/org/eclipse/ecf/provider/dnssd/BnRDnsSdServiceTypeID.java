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
package org.eclipse.ecf.provider.dnssd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

/**
 * This ServiceType represents the special RRs defined in 
 * chapter 12. Discovery of Browsing and Registration Domains
 * 
 * see http://files.dns-sd.org/draft-cheshire-dnsext-dns-sd.txt
 */
public class BnRDnsSdServiceTypeID extends DnsSdServiceTypeID {
	private static final long serialVersionUID = -466458565598238072L;

  	/**
  	 * A list of domains recommended for browsing
  	 */
  	static final String BROWSE_DOMAINS = "b._dns-sd"; //$NON-NLS-1$
	/**
	 * A single recommended default domain for browsing
	 */
	static final String DEFAULT_BROWSE_DOMAIN = "db._dns-sd"; //$NON-NLS-1$
	/**
	 * A list of domains recommended for registering services using Dynamic Update
	 */
	static final String REG_DOMAINS = "r._dns-sd"; //$NON-NLS-1$
	/**
	 * A single recommended default domain for registering services.
	 */
	static final String DEFAULT_REG_DOMAIN = "dr._dns-sd"; //$NON-NLS-1$

	BnRDnsSdServiceTypeID(IServiceTypeID aServiceType, String aService) {
		super(aServiceType.getNamespace(), aServiceType);
		services = new String[] {aService};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.dnssd.DnsSdServiceTypeID#getInternalQueries()
	 */
	Lookup[] getInternalQueries() {
		List result = new ArrayList();
		for (int i = 0; i < scopes.length; i++) {
			String scope = scopes[i];
			// remove dangling "."
			if(scope.endsWith(".")) { //$NON-NLS-1$
				scope = scope.substring(0, scope.length() - 1);
			}
			Lookup query;
			try {
				query = new Lookup(services[0] + "._udp" + "." + scope + ".", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						Type.PTR);
			} catch (TextParseException e) {
				continue;
			}
			result.add(query);
		}
		return (Lookup[]) result.toArray(new Lookup[result.size()]);
	}
	
	void setScope(String target) {
		if(target.endsWith(".")) { //$NON-NLS-1$
			target = target.substring(0, target.length() - 1);
		}
		scopes = new String[]{target};
		createType();
	}

	Collection getScopesAsZones() {
		final List res = new ArrayList(scopes.length);
		for (int i = 0; i < scopes.length; i++) {
			String scope = scopes[i];
			res.add(scope + "."); //$NON-NLS-1$
		}
		return res;
	}
}
