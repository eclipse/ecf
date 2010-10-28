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

import org.eclipse.ecf.discovery.IDiscoveryLocator;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.IServiceProperties;
import org.eclipse.ecf.discovery.identity.IServiceID;

public abstract class AbstractEndpointDescriptionFactory extends
		AbstractMetadataFactory implements IEndpointDescriptionFactory {

	protected List<DiscoveredEndpointDescription> discoveredEndpointDescriptions = new ArrayList();

	private DiscoveredEndpointDescription findDiscoveredEndpointDescription(
			EndpointDescription endpointDescription) {
		synchronized (discoveredEndpointDescriptions) {
			for (DiscoveredEndpointDescription d : discoveredEndpointDescriptions) {
				EndpointDescription ed = d.getEndpointDescription();
				if (ed.equals(endpointDescription))
					return d;
			}
		}
		return null;
	}

	public DiscoveredEndpointDescription createDiscoveredEndpointDescription(
			IDiscoveryLocator locator, IServiceInfo discoveredServiceInfo) {
		try {
			EndpointDescription endpointDescription = createEndpointDescription(
					locator, discoveredServiceInfo);
			synchronized (discoveredEndpointDescriptions) {
				DiscoveredEndpointDescription ded = findDiscoveredEndpointDescription(endpointDescription);
				if (ded != null) return ded;
				else return createDiscoveredEndpointDescription(locator,discoveredServiceInfo,endpointDescription);
			}
		} catch (Exception e) {
			logError("createDiscoveredEndpointDescription",
					"Exception creating discovered endpoint description", e);
			return null;
		}
	}

	public DiscoveredEndpointDescription getUndiscoveredEndpointDescription(
			IDiscoveryLocator locator, IServiceID serviceID) {
		// XXX todo
		return null;
	}

	protected EndpointDescription createEndpointDescription(
			IDiscoveryLocator locator, IServiceInfo discoveredServiceInfo) {
		IServiceProperties discoveredServiceProperties = discoveredServiceInfo
				.getServiceProperties();
		Map props = decodeServiceProperties(discoveredServiceProperties);
		return new EndpointDescription(props);

	}

	protected DiscoveredEndpointDescription createDiscoveredEndpointDescription(
			IDiscoveryLocator locator, IServiceInfo discoveredServiceInfo,
			EndpointDescription endpointDescription) {
		return new DiscoveredEndpointDescription(
				locator.getServicesNamespace(), discoveredServiceInfo.getServiceID(), endpointDescription);
	}


	public void close() {
		synchronized (discoveredEndpointDescriptions) {
			discoveredEndpointDescriptions.clear();
		}
		super.close();
	}
}
