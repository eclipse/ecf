/*******************************************************************************
 * Copyright (c) 2009 Markus Alexander Kuppe.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Alexander Kuppe (ecf-dev_eclipse.org <at> lemmster <dot> de) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.dnssd;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.discovery.identity.ServiceTypeID;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

public class DnsSdServiceTypeID extends ServiceTypeID implements IServiceTypeID {

	private static final long serialVersionUID = 1247933069737880365L;

	public DnsSdServiceTypeID(Namespace ns, IServiceTypeID id) {
		super(ns, id);
	}

	public DnsSdServiceTypeID(Namespace namespace, String aType)
			throws IDCreateException {
		super(namespace, aType);
	}

	public DnsSdServiceTypeID(Namespace namespace, String[] services,
			String[] scopes, String[] protocols, String namingAuthority) {
		super(namespace, services, scopes, protocols, namingAuthority);
	}

	public DnsSdServiceTypeID(Namespace namespace) {
		super(namespace);
		try {
			final InetAddress localHost = InetAddress.getLocalHost();
			final String fqdn = localHost.getCanonicalHostName();
			int idx = fqdn.indexOf(".");
			if(idx > -1) {
				scopes = new String[]{fqdn.substring(idx +1)};
			} else {
				scopes = new String[]{fqdn};
			}
		} catch (UnknownHostException e) {
			scopes = null;
		}
	}
	
	DnsSdServiceTypeID() {
		super(new DnsSdNamespace());
	}

	DnsSdServiceTypeID(Namespace namespace, Name aName) {
		super(namespace, aName.toString());
	}

	Lookup[] getInternalQueries() {
		String[] protos = protocols;
		int type = Type.SRV;

		String service = null;
		if (services == null || services.length == 0
				|| (services.length == 1 && services[0].equals(""))) {
			// if no service is set, create a non service specific query
			service = "_services._dns-sd._";

			// and set proto to "udp" irregardless what has been set
			protos = new String[] { "udp" };

			// and query for PTR records
			type = Type.PTR;
		} else {
			service = "_";
			for (int i = 0; i < services.length; i++) {
				service += services[i];
				service += "._";
			}
		}

		List result = new ArrayList();
		for (int i = 0; i < scopes.length; i++) {
			String scope = scopes[i];
			for (int j = 0; j < protos.length; j++) {
				Lookup query;
				try {
					query = new Lookup(service + protos[j] + "." + scope + ".",
							type);
				} catch (TextParseException e) {
					continue;
				}
				result.add(query);
			}
		}
		return (Lookup[]) result.toArray(new Lookup[result.size()]);
	}

	public void setScopes(Name[] searchPaths) {
		String[] s = new String[searchPaths.length];
		for(int i = 0; i < searchPaths.length; i++) {
			s[i] = searchPaths[i].toString();
		}
		scopes = s;
	}
}
