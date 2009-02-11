/*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.discovery;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.osgi.service.discovery.DiscoveredServiceNotification;
import org.osgi.service.discovery.ServiceEndpointDescription;

public class DiscoveredServiceNotificationImpl implements
		DiscoveredServiceNotification {

	private final int type;
	private ServiceEndpointDescription serviceEndpointDescription;

	public DiscoveredServiceNotificationImpl(ID localContainerID, int type,
			IServiceInfo serviceInfo) {
		this.type = type;
		this.serviceEndpointDescription = new ServiceEndpointDescriptionImpl(
				localContainerID, serviceInfo);
	}

	public ServiceEndpointDescription getServiceEndpointDescription() {
		return serviceEndpointDescription;
	}

	public int getType() {
		return type;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("DiscoveredServiceNotificationImpl[");
		sb.append("type=").append(getType()).append(";sed=").append(
				getServiceEndpointDescription()).append("]");
		return sb.toString();
	}
}
