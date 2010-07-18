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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.events.ContainerConnectedEvent;
import org.eclipse.ecf.core.events.ContainerConnectingEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.discovery.DiscoveryContainerConfig;
import org.eclipse.ecf.discovery.IServiceInfo;
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
			// TYPE.SRV
			final Map srvRecords = serviceID.toSRVRecords();
			for (Iterator itr = srvRecords.entrySet().iterator(); itr.hasNext(); ) {
				final Map.Entry entry = (Entry) itr.next();
				final Name zone = (Name) entry.getKey();
				final Record record = (Record) entry.getValue();
				final Update update = new Update(zone);
				if(mode == ADD) {
					//TODO add absent/present condition checks
					update.replace(record);
				} else {
					update.delete(record);
				}
				
				// TYPE.TXT
				final Record[] txtRecords = serviceID.toTXTRecords(record.getName(), zone);
				for (int j = 0; j < txtRecords.length; j++) {
					if(mode == ADD) {
						update.add(txtRecords[j]);
					} else {
						update.delete(txtRecords[j]);
					}
				}
				
				// set up a new resolver for the given domain (a scope might use different domains)
				((SimpleResolver)resolver).setAddress(InetAddress.getByName("ns1.ecf-project.org"));
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
