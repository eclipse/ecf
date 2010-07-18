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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.discovery.IServiceProperties;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.discovery.identity.ServiceID;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

public class DnsSdServiceID extends ServiceID {

	private static final long serialVersionUID = -5265675009221335638L;

	protected DnsSdServiceID(final Namespace namespace, final IServiceTypeID type, final URI anUri) {
		super(namespace, type, anUri);
	}

	// TYPE.SRV
	public Map toSRVRecords() throws IOException {
		final Map result = new HashMap();
		final String[] scopes = type.getScopes();
		for (int i = 0; i < scopes.length; i++) {
			final String domain = scopes[i] + ".";
			final Name zone = Name.fromString(domain);

			final Name name = Name.fromString("_" + type.getServices()[0] + "._"
					+ type.getProtocols()[0], zone);

			final long ttl = serviceInfo.getTTL();
			final int priority = serviceInfo.getPriority();
			final int port = serviceInfo.getLocation().getPort();
			final String target = serviceInfo.getLocation().getHost();
			final int weight = serviceInfo.getWeight();

			result.put(zone, Record.fromString(name, Type.SRV, DClass.IN,
					ttl, priority + " " + weight + " " + port + " " + target
							+ ".", zone));
		}
		return result;
	}

	// TYPE.TXT
	public Record[] toTXTRecords(final Name name, final Name zone) throws IOException {
		final List result = new ArrayList();
		final IServiceProperties properties = serviceInfo.getServiceProperties();
		final Enumeration enumeration = properties.getPropertyNames();
		while (enumeration.hasMoreElements()) {
			final Object property = enumeration.nextElement();
			final String key = property.toString();
			final String value = (String) properties.getProperty(key).toString();
			result.add(Record.fromString(name, Type.TXT, DClass.IN,
					serviceInfo.getTTL(), key + "=" + value, zone));
		}
		return (Record[]) result.toArray(new Record[result.size()]);
	}
}
