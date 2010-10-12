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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.ServiceProperties;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import static org.osgi.service.remoteserviceadmin.RemoteConstants.*;

public class DefaultEndpointDescriptionFactory implements IEndpointDescriptionFactory {

	private List discoveredECFEndpointDescriptions = new ArrayList();
	
	public EndpointDescription createDiscoveredEndpointDescription(
			IServiceID serviceID, IServiceInfo serviceInfo) {
		Assert.isNotNull(serviceID);
		Assert.isNotNull(serviceInfo);
		return getECFEndpointDescription(serviceID, serviceInfo);
	}

	protected void logWarning(String message) {
		// XXX change to log
		System.out.println(message);
	}
	
	protected ECFEndpointDescription getECFEndpointDescription(IServiceID serviceID, IServiceInfo serviceInfo) {
		// XXX todo
		return null;
	}
	
	private Map createOSGiPropertiesFromDiscoveredServiceInfo(
			IServiceInfo discoveredServiceInfo) {
		Properties discoveredProperties = ((ServiceProperties) discoveredServiceInfo.getServiceProperties()).asProperties();
		Map props = new TreeMap(
				String.CASE_INSENSITIVE_ORDER);
		// XXX we need to make sure the EndpointDescription-required properties
		// are set...i.e. 
		// id/RemoteConstants.ENDPOINT_ID
		// frameworkUUID/RemoteConstants.ENDPOINT_FRAMEWORK_UUID
		// serviceId/RemoteConstants.ENDPOINT_SERVICE_ID
		// interfaces/org.osgi.framework.Constants.OBJECTCLASS
		props.put(ENDPOINT_ID, "myEndpointId");
		props.put(ENDPOINT_SERVICE_ID, "myEndpointServiceId");
		props.put(ENDPOINT_SERVICE_ID, 1L);
		props.put(org.osgi.framework.Constants.OBJECTCLASS, new String[] { "myserviceinterface" });
		props.putAll(discoveredProperties);
		return props;
	}

	public EndpointDescription getUndiscoveredEndpointDescription(
			IServiceID serviceId, IServiceInfo undiscoveredServiceInfo) {
		// XXX todo
		return null;
	}

}
