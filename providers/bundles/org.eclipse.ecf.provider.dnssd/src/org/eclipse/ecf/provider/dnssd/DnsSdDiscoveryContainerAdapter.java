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
import org.eclipse.ecf.core.events.ContainerDisconnectedEvent;
import org.eclipse.ecf.core.events.ContainerDisconnectingEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.discovery.AbstractDiscoveryContainerAdapter;
import org.eclipse.ecf.discovery.DiscoveryContainerConfig;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TSIG;

public abstract class DnsSdDiscoveryContainerAdapter extends
		AbstractDiscoveryContainerAdapter {

	protected Resolver resolver;
	protected DnsSdServiceTypeID targetID;

	public DnsSdDiscoveryContainerAdapter(String aNamespaceName,
			DiscoveryContainerConfig aConfig) {
		super(aNamespaceName, aConfig);
	}

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

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryAdvertiser#registerService(org.eclipse.ecf.discovery.IServiceInfo)
	 */
	public void registerService(IServiceInfo serviceInfo) {
		Assert.isNotNull(serviceInfo);
		// nop, we are just a Locator but AbstractDiscoveryContainerAdapter
		// doesn't support this yet
		throw new UnsupportedOperationException(
				"This is not an IDiscoveryAdvertiser");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryAdvertiser#unregisterService(org.eclipse.ecf.discovery.IServiceInfo)
	 */
	public void unregisterService(IServiceInfo serviceInfo) {
		Assert.isNotNull(serviceInfo);
		// nop, we are just a Locator but AbstractDiscoveryContainerAdapter
		// doesn't support this yet
		throw new UnsupportedOperationException(
				"This is not an IDiscoveryAdvertiser");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#connect(org.eclipse.ecf.core.identity.ID, org.eclipse.ecf.core.security.IConnectContext)
	 */
	public abstract void connect(ID targetID, IConnectContext connectContext)
			throws ContainerConnectException;

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#getConnectedID()
	 */
	public ID getConnectedID() {
		return targetID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#disconnect()
	 */
	public void disconnect() {
		fireContainerEvent(new ContainerDisconnectingEvent(this.getID(),
				getConnectedID()));
		targetID = null;
		fireContainerEvent(new ContainerDisconnectedEvent(this.getID(),
				getConnectedID()));
	}

	/**
	 * @param searchPaths The default search path used for discovery 
	 */
	public void setSearchPath(String[] searchPaths) {
		targetID.setSearchPath(searchPaths);
	}

	/**
	 * @return The default search path used by this discovery provider
	 */
	public String[] getSearchPath() {
		return targetID.getSearchPath();
	}

	/**
	 * @param aResolver The resolver to use
	 * @throws DnsSdDiscoveryException if hostname cannot be resolved
	 */
	public void setResolver(String aResolver) {
		try {
			resolver = new SimpleResolver(aResolver);
		} catch (UnknownHostException e) {
			throw new DnsSdDiscoveryException(e);
		}
	}

	/**
	 * @param tsigKeyName A key name/user name for dns dynamic update
	 * @param tsigKey A string representation of the shared key
	 */
	public void setTsigKey(String tsigKeyName, String tsigKey) {
		resolver.setTSIGKey(new TSIG(tsigKeyName, tsigKey));
	}
}
