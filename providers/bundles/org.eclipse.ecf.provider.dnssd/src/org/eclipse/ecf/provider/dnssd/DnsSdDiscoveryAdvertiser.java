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

import java.io.EOFException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.events.ContainerConnectedEvent;
import org.eclipse.ecf.core.events.ContainerConnectingEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.discovery.DiscoveryContainerConfig;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Message;
import org.xbill.DNS.NSRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.SOARecord;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;
import org.xbill.DNS.Update;

public class DnsSdDiscoveryAdvertiser extends DnsSdDiscoveryContainerAdapter {
	
	private static final String _DNS_UPDATE = "_dns-update._udp.";
	private static final boolean ADD = true;
	private static final boolean REMOVE = false;

	public DnsSdDiscoveryAdvertiser() {
		super(DnsSdNamespace.NAME, new DiscoveryContainerConfig(IDFactory
				.getDefault().createStringID(
						DnsSdDiscoveryAdvertiser.class.getName())));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.dnssd.DnsSdDiscoveryLocator#registerService(org.eclipse.ecf.discovery.IServiceInfo)
	 */
	public void registerService(IServiceInfo serviceInfo) {
		sendToServer(serviceInfo, ADD); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.dnssd.DnsSdDiscoveryLocator#unregisterService(org.eclipse.ecf.discovery.IServiceInfo)
	 */
	public void unregisterService(IServiceInfo serviceInfo) {
		sendToServer(serviceInfo, REMOVE);
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
		// purge cache means renew resolver?
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
					resolver.setTCP(true);
				} catch (UnknownHostException e) {
					throw new ContainerConnectException(e);
				}
			}

			// done setting up this provider, send event
			fireContainerEvent(new ContainerConnectingEvent(this.getID(), targetID,
					connectContext));
			fireContainerEvent(new ContainerConnectedEvent(this.getID(), targetID));
	}

	private void sendToServer(final IServiceInfo serviceInfo, final boolean mode) {
		Assert.isNotNull(serviceInfo);
		final DnsSdServiceID serviceID = (DnsSdServiceID) serviceInfo.getServiceID();
		try {
			final Record srvRecord = serviceID.toSRVRecord(); // TYPE.SRV
			final Record[] txtRecords = serviceID.toTXTRecords(srvRecord); // TYPE.TXT
			final Name name = serviceID.getDnsName();
		
			final String[] registrationDomains = getRegistrationDomains(serviceID.getServiceTypeID());
		
			for (int i = 0; i < registrationDomains.length; i++) {
				final Name zone = new Name(registrationDomains[i]);
				final Name fqdn = new Name(name.toString() + "." + zone.toString());
				final Update update = new Update(zone);

				//TYPE.SRV
				if(mode == ADD) {
					//TODO add absent/present condition checks
					update.replace(srvRecord.withName(fqdn));
				} else {
					update.delete(srvRecord.withName(fqdn));
				}
				
				//TYPE.TXT
				for (int j = 0; j < txtRecords.length; j++) {
					if(mode == ADD) {
						update.add(txtRecords[j].withName(fqdn));
					} else {
						update.delete(txtRecords[j].withName(fqdn));
					}
				}
				
				// set up a the resolver for the given domain (a scope might use different domains)
				final Collection dnsServers = getUpdateDomain(zone);
				if(dnsServers.size() == 0) {
					throw new DnsSdDiscoveryException("No server for dnsupdate could be found");
				}
				for (final Iterator iterator = dnsServers.iterator(); iterator.hasNext();) {
					final SRVRecord dnsServer = (SRVRecord) iterator.next();
					
					// try to send msg and fail gracefully if more dns servers are available
					final Name target = dnsServer.getTarget();
					final Message response;
					final InetAddress byName;
					try {
						byName = InetAddress.getByName(target.toString());

						((SimpleResolver) resolver).setAddress(byName);
						((SimpleResolver) resolver).setPort(dnsServer.getPort());
						
						response = resolver.send(update);
					} catch (UnknownHostException uhe) {
						if(iterator.hasNext()) {
							continue;
						} else {
							throw new DnsSdDiscoveryException(uhe);
						}
					} catch (EOFException eof) {
						if(iterator.hasNext()) {
							continue;
						} else {
							throw new DnsSdDiscoveryException(eof);
						}
					}
					
					// catch some errors and fall back to the next dnsServer
					if (response.getRcode() != Rcode.NOERROR) {
						if(iterator.hasNext()) {
							continue;
						} else {
							throw DnsSdDiscoveryException.getException(response.getRcode());
						}
					}
				}
			}
		} catch (Exception e) {
			throw new DnsSdDiscoveryException(e);
		}
	}

	private Collection getUpdateDomain(final Name zone) throws TextParseException {
		// query for special "_dns-update" SRV records which mark the server to use for dyndns
		final Lookup query = new Lookup(_DNS_UPDATE + zone, Type.SRV);
		// use the SRV record with the lowest priority/weight first
		final SortedSet srvRecords = getSRVRecord(query, new SRVRecordComparator());
//
//		int prio = Integer.MAX_VALUE, weight = Integer.MAX_VALUE;
//		for (Iterator iterator = srvRecords.iterator(); iterator.hasNext();) {
//			final SRVRecord srvRecord = (SRVRecord) iterator.next();
//			if(srvRecord.getPriority() < prio) {
//				if (srvRecord.getWeight() < weight) {
//					prio = srvRecord.getPriority();
//					weight = srvRecord.getPriority();
//					result.add(new URI("dns://" + srvRecord.getTarget() + ":" + srvRecord.getPort()));
//				}
//			}
//		}

		// if no dedicated "_dns-update" server is configured, fall back to regular authoritative server
		if(srvRecords.size() == 0) {
			return getAuthoritativeNameServer(zone);
		}
		return srvRecords; 
	}
	
	private Collection getAuthoritativeNameServer(final Name zone) throws TextParseException {
		final Set result = new HashSet();
		final Name name = new Name(_DNS_UPDATE + zone);
		
		//query for NS records
		Lookup query = new Lookup(zone, Type.NS);
		query.setResolver(resolver);
		Record[] queryResult = query.run();
		//TODO file bug upstream that queryResult may never be null
		int length = queryResult == null ? 0 : queryResult.length;
		for (int j = 0; j < length; j++) {
			final Record record = queryResult[j];
			if(record instanceof NSRecord) {
				final NSRecord nsRecord = (NSRecord) record;
				final Name target = nsRecord.getTarget();
				result.add(new SRVRecord(name, DClass.IN, nsRecord.getTTL(), 0, 0, SimpleResolver.DEFAULT_PORT, target));
			}
		}
		
		//query for primary ns in SOA record (may overwrite/be equal to one of the ns records)
		query = new Lookup(zone, Type.SOA);
		query.setResolver(resolver);
		queryResult = query.run();
		//TODO file bug upstream that queryResult may never be null
		length = queryResult == null ? 0 : queryResult.length;
		for (int j = 0; j < length; j++) {
			final Record record = queryResult[j];
			if(record instanceof SOARecord) {
				final SOARecord soaRecord = (SOARecord) record;
				result.add(new SRVRecord(name, DClass.IN, soaRecord.getTTL(), 0, 0, SimpleResolver.DEFAULT_PORT, soaRecord.getHost()));
			}
		}
		return result; 
	}

	private String[] getRegistrationDomains(IServiceTypeID aServiceTypeId) {
		String[] rrs = new String[] {BnRDnsSdServiceTypeID.REG_DOMAINS, BnRDnsSdServiceTypeID.DEFAULT_REG_DOMAIN};
		final String[] registrationDomains = getBrowsingOrRegistrationDomains(aServiceTypeId, rrs);
		String[] scopes = aServiceTypeId.getScopes();
		for (int i = 0; i < scopes.length; i++) {
			scopes[i] = scopes[i].concat(".");
		}
		return registrationDomains.length == 0 ? scopes : registrationDomains; 
	}
}
