/****************************************************************************
 * Copyright (c) 2015 Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Scott Lewis - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteserviceadmin.ui.endpoint.model;

import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.internal.remoteservices.ui.Messages;

/**
 * @since 3.3
 */
public class EndpointDiscoveryGroupNode extends EndpointGroupNode {

	public EndpointDiscoveryGroupNode(String groupName, IServiceID serviceID) {
		super(groupName);
		addChild(new EndpointNameValueNode(Messages.EndpointDiscoveryGroupNode_DISCOVERY_LOCATION,
				serviceID.getLocation().toString()));
		addChild(new EndpointNameValueNode(Messages.EndpointDiscoveryGroupNode_DISCOVERY_SERVICE_TYPE_ID,
				serviceID.getServiceTypeID().getName()));
		addChild(new EndpointNameValueNode(Messages.EndpointDiscoveryGroupNode_DISCOVERY_NAMESPACE,
				serviceID.getNamespace().getName()));
	}

}
