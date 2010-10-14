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

public class DefaultEndpointDescriptionFactory implements IEndpointDescriptionFactory {

	protected void logWarning(String message) {
		// XXX change to log
		System.out.println(message);
	}
	
	protected EndpointDescription getECFEndpointDescription(IServiceID serviceID, IServiceInfo serviceInfo) {
		// XXX todo
		return null;
	}
	
	public EndpointDescription getUndiscoveredEndpointDescription(
			IServiceID serviceId, IServiceInfo undiscoveredServiceInfo) {
		// XXX todo
		return null;
	}

	public EndpointDescription createDiscoveredEndpointDescription(
			IServiceInfo discoveredServiceInfo) {
		// TODO Auto-generated method stub
		return null;
	}

}
