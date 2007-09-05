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

package org.eclipse.ecf.provider.jmdns.identity;

import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.discovery.identity.ServiceTypeID;
import org.eclipse.osgi.util.NLS;

/**
 * 
 */
public class JMDNSServiceTypeID extends ServiceTypeID {

	private static final long serialVersionUID = 7549266915001431139L;

	protected JMDNSServiceTypeID(Namespace namespace, String type) throws IDCreateException {
		super(namespace, type);
		try {
			parse(type);
		} catch (final Exception e) {
			throw new IDCreateException(NLS.bind("{0} is not a valid JMDNSServiceTypeID", type));
		}
	}

	// Format of an DNS SRV RR _Service._Proto.Name
	// e.g. _ecftcp._tcp.local.
	// e.g. _service._dns-srv._udp.ecf.eclipse.org.
	private void parse(String type) {
		this.namingAuthority = "IANA";
		final int protoBegin = type.lastIndexOf("_");
		final int protoEnd = type.indexOf(".", protoBegin);
		protocols = new String[] {type.substring(protoBegin + 1, protoEnd)};
		scopes = new String[] {type.substring(protoEnd + 1, type.length() - 1)};
		String servicesString = type.substring(0, protoBegin - 1);
		servicesString = servicesString.substring(1);
		services = servicesString.split("._");
	}
}
