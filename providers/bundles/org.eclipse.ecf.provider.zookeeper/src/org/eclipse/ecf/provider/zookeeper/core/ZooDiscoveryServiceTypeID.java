/****************************************************************************
 * Copyright (c)2010 REMAIN B.V. The Netherlands. (http://www.remainsoftware.com).
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 *  Contributors:
 *    Wim Jongman - initial API and implementation 
 *    Ahmed Aadel - initial API and implementation     
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.provider.zookeeper.core;

import java.util.UUID;

import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.discovery.identity.ServiceTypeID;

public class ZooDiscoveryServiceTypeID extends ServiceTypeID {

	private static final long serialVersionUID = 9063908479280524897L;
	private String id;

	public ZooDiscoveryServiceTypeID(ZooDiscoveryNamespace discoveryNamespace,
			IServiceTypeID typeId) {
		super(discoveryNamespace, typeId);
		this.id = UUID.randomUUID().toString();
	}

	public ZooDiscoveryServiceTypeID(ZooDiscoveryNamespace discoveryNamespace,
			IServiceTypeID typeId, String internal) {
		super(discoveryNamespace, typeId);
		this.id = internal;
	}

	public String getInternal() {
		return this.id;
	}

}
