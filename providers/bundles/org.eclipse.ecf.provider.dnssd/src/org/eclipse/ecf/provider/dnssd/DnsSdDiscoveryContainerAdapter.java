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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TSIG;
import org.xbill.DNS.Type;

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

	protected String[] getBrowsingOrRegistrationDomains(final IServiceTypeID aServiceTypeId, final String[] rrs) {
		final Set res = new HashSet();
		for (int i = 0; i < rrs.length; i++) {
			final BnRDnsSdServiceTypeID serviceType = 
				new BnRDnsSdServiceTypeID(aServiceTypeId, rrs[i]);
			
			final Record[] records = getRecords(serviceType);
			for (int j = 0; j < records.length; j++) {
				final PTRRecord record = (PTRRecord) records[j];
				res.add(record.getTarget().toString());
			}
		}
		
		return (String[]) res.toArray(new String[res.size()]);
	}
	
	protected Record[] getRecords(final DnsSdServiceTypeID serviceTypeId) {
		final List result = new ArrayList();
		final Lookup[] queries = serviceTypeId.getInternalQueries();
		for (int i = 0; i < queries.length; i++) {
			final Lookup query = queries[i];
			query.setResolver(resolver);
			final Record[] queryResult = query.run();
			if(queryResult != null) {
				result.addAll(Arrays.asList(queryResult));
			}
		}
		return (Record[]) result.toArray(new Record[result.size()]);
	}

	protected List getSRVRecords(Lookup[] queries) {
		List srvRecords = new ArrayList();
		for (int i = 0; i < queries.length; i++) {
			srvRecords.addAll(getSRVRecord(queries[i]));
		}
		return srvRecords;
	}

	protected List getSRVRecord(Lookup query) {
		final List srvRecords = new ArrayList();
		query.setResolver(resolver);
		final Record[] queryResult = query.run();
		//TODO file bug upstream that queryResult may never be null
		final int length = queryResult == null ? 0 : queryResult.length;
		for (int j = 0; j < length; j++) {
			Record[] srvQueryResult = null;
			final Record record = queryResult[j];
			if(record instanceof PTRRecord) {
				final PTRRecord ptrRecord = (PTRRecord) record;
				final Name target = ptrRecord.getTarget();
				final Lookup srvQuery = new Lookup(target, Type.SRV);
				srvQuery.setResolver(resolver);
				srvQueryResult = srvQuery.run();
			} else if (record instanceof SRVRecord) {
				srvQueryResult = new SRVRecord[]{(SRVRecord) record};
			}
			srvRecords.addAll(Arrays.asList(srvQueryResult));
		}
		return srvRecords;
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
