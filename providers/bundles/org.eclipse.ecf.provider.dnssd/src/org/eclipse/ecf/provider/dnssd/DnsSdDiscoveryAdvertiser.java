/*******************************************************************************
 * Copyright (c) 2010 Markus Alexander Kuppe.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Alexander Kuppe (ecf-dev_eclipse.org <at> lemmster <dot> de) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.dnssd;

import java.net.UnknownHostException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.events.ContainerConnectedEvent;
import org.eclipse.ecf.core.events.ContainerConnectingEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.xbill.DNS.SimpleResolver;

public class DnsSdDiscoveryAdvertiser extends DnsSdDiscoveryLocator {

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.dnssd.DnsSdDiscoveryLocator#registerService(org.eclipse.ecf.discovery.IServiceInfo)
	 */
	public void registerService(IServiceInfo serviceInfo) {
		Assert.isNotNull(serviceInfo);
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.dnssd.DnsSdDiscoveryLocator#unregisterService(org.eclipse.ecf.discovery.IServiceInfo)
	 */
	public void unregisterService(IServiceInfo serviceInfo) {
		Assert.isNotNull(serviceInfo);
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.AbstractDiscoveryContainerAdapter#unregisterAllServices()
	 */
	public void unregisterAllServices() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.AbstractDiscoveryContainerAdapter#purgeCache()
	 */
	public IServiceInfo[] purgeCache() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.dnssd.DnsSdDiscoveryLocator#connect(org.eclipse.ecf.core.identity.ID, org.eclipse.ecf.core.security.IConnectContext)
	 */
	public void connect(ID aTargetID, IConnectContext connectContext)
			throws ContainerConnectException {

			// connect can only be called once
			if (targetID != null || getConfig() == null) {
				throw new ContainerConnectException("Already connected");
			}

			//TODO convert non DnsSdServiceTypeIDs into DSTIDs
			if(aTargetID == null) {
				targetID = new DnsSdServiceTypeID();
			} else {
				targetID = (DnsSdServiceTypeID) aTargetID;
			}
			
			// instantiate a default resolver
			if(resolver == null) {
				try {
					resolver = new SimpleResolver();
				} catch (UnknownHostException e) {
					throw new ContainerConnectException(e);
				}
			}

			// done setting up this provider, send event
			fireContainerEvent(new ContainerConnectingEvent(this.getID(), targetID,
					connectContext));
			fireContainerEvent(new ContainerConnectedEvent(this.getID(), targetID));
	}

	/* not a locator! */

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.dnssd.DnsSdDiscoveryLocator#getServiceInfo(org.eclipse.ecf.discovery.identity.IServiceID)
	 */
	public IServiceInfo getServiceInfo(IServiceID aServiceId) {
		Assert.isNotNull(aServiceId);
		// nop, we are just an Advertiser but AbstractDiscoveryContainerAdapter
		// doesn't support this yet
		throw new UnsupportedOperationException(
				"This is not an IDiscoveryLocator");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.dnssd.DnsSdDiscoveryLocator#getServiceTypes()
	 */
	public IServiceTypeID[] getServiceTypes() {
		// nop, we are just an Advertiser but AbstractDiscoveryContainerAdapter
		// doesn't support this yet
		throw new UnsupportedOperationException(
				"This is not an IDiscoveryLocator");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.dnssd.DnsSdDiscoveryLocator#getServices()
	 */
	public IServiceInfo[] getServices() {
		// nop, we are just an Advertiser but AbstractDiscoveryContainerAdapter
		// doesn't support this yet
		throw new UnsupportedOperationException(
				"This is not an IDiscoveryLocator");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.dnssd.DnsSdDiscoveryLocator#getServices(org.eclipse.ecf.discovery.identity.IServiceTypeID)
	 */
	public IServiceInfo[] getServices(IServiceTypeID aServiceTypeId) {
		Assert.isNotNull(aServiceTypeId);
		// nop, we are just an Advertiser but AbstractDiscoveryContainerAdapter
		// doesn't support this yet
		throw new UnsupportedOperationException(
				"This is not an IDiscoveryLocator");
	}
}
