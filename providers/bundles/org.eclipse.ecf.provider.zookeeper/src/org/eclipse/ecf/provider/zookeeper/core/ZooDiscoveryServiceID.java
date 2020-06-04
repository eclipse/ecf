/****************************************************************************
 * Copyright (c)2010 REMAIN B.V. The Netherlands. (http://www.remainsoftware.com).
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 *  Contributors:
 *     Wim Jongman - initial API and implementation 
 *     Ahmed Aadel - initial API and implementation     
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.provider.zookeeper.core;

import java.net.URI;

import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.discovery.identity.ServiceID;

public class ZooDiscoveryServiceID extends ServiceID {

	private static final long serialVersionUID = 1185925207835288995L;

	protected ZooDiscoveryServiceID(Namespace namespace, IServiceTypeID type,
			URI anURI) {
		super(namespace, type, anURI);
	}

	public ZooDiscoveryServiceID(Namespace namespace,
			DiscoverdService discoverdService, IServiceTypeID type,
			URI anURI) {
		this(namespace, type, anURI);
		setServiceInfo(discoverdService);
	}
}
