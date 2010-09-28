/*******************************************************************************
* Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.osgi.service.remoteserviceadmin.EndpointDescription;

public interface IEndpointDescriptionFactory {

	public EndpointDescription createDiscoveredEndpointDescription(IServiceID serviceId, IServiceInfo discoveredServiceInfo);
	public EndpointDescription createUndiscoveredEndpointDescription(IServiceID serviceId, IServiceInfo undiscoveredServiceInfo);
	
}
