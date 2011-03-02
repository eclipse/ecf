/*******************************************************************************
 * Copyright (c) 2010-2011 Composent, Inc. and others. All rights reserved. This
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

import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.discovery.IDiscoveryLocator;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.IServiceProperties;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.osgi.service.remoteserviceadmin.EndpointDescription;

/**
 * Default implementation of {@link IDiscoveredEndpointDescriptionFactory}
 * service.
 * 
 * @see IDiscoveredEndpointDescriptionFactory
 */
public class DiscoveredEndpointDescriptionFactory extends
		AbstractMetadataFactory implements
		IDiscoveredEndpointDescriptionFactory {

	protected List<DiscoveredEndpointDescription> discoveredEndpointDescriptions = new ArrayList();

	private DiscoveredEndpointDescription findDiscoveredEndpointDescription(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription) {
		synchronized (discoveredEndpointDescriptions) {
			for (DiscoveredEndpointDescription d : discoveredEndpointDescriptions) {
				org.osgi.service.remoteserviceadmin.EndpointDescription ed = d
						.getEndpointDescription();
				if (ed.equals(endpointDescription))
					return d;
			}
		}
		return null;
	}

	private DiscoveredEndpointDescription findUniscoveredEndpointDescription(
			IDiscoveryLocator locator, IServiceID serviceID) {
		synchronized (discoveredEndpointDescriptions) {
			for (DiscoveredEndpointDescription d : discoveredEndpointDescriptions) {
				Namespace dln = d.getDiscoveryLocatorNamespace();
				IServiceID svcId = d.getServiceID();
				if (dln.getName().equals(
						locator.getServicesNamespace().getName())
						&& svcId.equals(serviceID)) {
					return d;
				}
			}
		}
		return null;
	}

	public DiscoveredEndpointDescription createDiscoveredEndpointDescription(
			IDiscoveryLocator locator, IServiceInfo discoveredServiceInfo) {
		try {
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription = createEndpointDescription(
					locator, discoveredServiceInfo);
			synchronized (discoveredEndpointDescriptions) {
				DiscoveredEndpointDescription ded = findDiscoveredEndpointDescription(endpointDescription);
				if (ded != null)
					return ded;
				else {
					ded = createDiscoveredEndpointDescription(locator,
							discoveredServiceInfo, endpointDescription);
					// put into discoveredEndpointDescriptions
					discoveredEndpointDescriptions.add(ded);
					return ded;
				}
			}
		} catch (Exception e) {
			logError("createDiscoveredEndpointDescription", //$NON-NLS-1$
					"Exception creating discovered endpoint description", e); //$NON-NLS-1$
			return null;
		}
	}

	public DiscoveredEndpointDescription removeDiscoveredEndpointDescription(
			IDiscoveryLocator locator, IServiceID serviceID) {
		synchronized (discoveredEndpointDescriptions) {
			DiscoveredEndpointDescription ded = findUniscoveredEndpointDescription(
					locator, serviceID);
			if (ded != null) {
				// remove
				discoveredEndpointDescriptions.remove(ded);
				return ded;
			}
		}
		return null;
	}

	protected org.osgi.service.remoteserviceadmin.EndpointDescription createEndpointDescription(
			IDiscoveryLocator locator, IServiceInfo discoveredServiceInfo) {
		IServiceProperties discoveredServiceProperties = discoveredServiceInfo
				.getServiceProperties();
		return decodeEndpointDescription(discoveredServiceProperties);

	}

	protected DiscoveredEndpointDescription createDiscoveredEndpointDescription(
			IDiscoveryLocator locator,
			IServiceInfo discoveredServiceInfo,
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription) {
		return new DiscoveredEndpointDescription(
				locator.getServicesNamespace(),
				discoveredServiceInfo.getServiceID(), endpointDescription);
	}

	public void close() {
		removeAllDiscoveredEndpointDescriptions();
		super.close();
	}

	public boolean removeDiscoveredEndpointDescription(
			EndpointDescription endpointDescription) {
		synchronized (discoveredEndpointDescriptions) {
			DiscoveredEndpointDescription d = findDiscoveredEndpointDescription(endpointDescription);
			if (d != null) {
				discoveredEndpointDescriptions.remove(d);
				return true;
			}
		}
		return false;
	}

	public void removeAllDiscoveredEndpointDescriptions() {
		synchronized (discoveredEndpointDescriptions) {
			discoveredEndpointDescriptions.clear();
		}
	}
}
