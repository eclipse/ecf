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

import java.net.InetAddress;
import java.net.UnknownHostException;

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
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Update;

public class DnsSdDiscoveryAdvertiser extends DnsSdDiscoveryContainerAdapter {
	
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
				
				// set up a new resolver for the given domain (a scope might use different domains)
				((SimpleResolver) resolver).setAddress(getUpdateDomain(zone));
				resolver.setTCP(true);
				final Message response = resolver.send(update);
				if(response.getRcode() != Rcode.NOERROR) {
					throw DnsSdDiscoveryException.getException(response.getRcode());
				}
			}
		} catch (Exception e) {
			throw new DnsSdDiscoveryException(e);
		}
	}

	private InetAddress getUpdateDomain(final Name zone) throws UnknownHostException {
		//TODO resolve dyndns domain
		return InetAddress.getByName("ns1.ecf-project.org");
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
				} catch (UnknownHostException e) {
					throw new ContainerConnectException(e);
				}
			}

			// done setting up this provider, send event
			fireContainerEvent(new ContainerConnectingEvent(this.getID(), targetID,
					connectContext));
			fireContainerEvent(new ContainerConnectedEvent(this.getID(), targetID));
	}
}
