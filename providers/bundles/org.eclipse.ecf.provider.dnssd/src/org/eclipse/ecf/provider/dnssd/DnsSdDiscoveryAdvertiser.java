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
import java.util.Enumeration;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.events.ContainerConnectedEvent;
import org.eclipse.ecf.core.events.ContainerConnectingEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.discovery.DiscoveryContainerConfig;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.IServiceProperties;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;
import org.xbill.DNS.Update;

public class DnsSdDiscoveryAdvertiser extends DnsSdDiscoveryContainerAdapter {

	public DnsSdDiscoveryAdvertiser() {
		super(DnsSdNamespace.NAME, new DiscoveryContainerConfig(IDFactory
				.getDefault().createStringID(
						DnsSdDiscoveryAdvertiser.class.getName())));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.dnssd.DnsSdDiscoveryLocator#registerService(org.eclipse.ecf.discovery.IServiceInfo)
	 */
	public void registerService(IServiceInfo serviceInfo) {
		Assert.isNotNull(serviceInfo);
		DnsSdServiceID serviceID = (DnsSdServiceID) serviceInfo.getServiceID();
		DnsSdServiceTypeID serviceTypeID = (DnsSdServiceTypeID) serviceID.getServiceTypeID();

		String[] scopes = serviceTypeID.getScopes();
		for (int i = 0; i < scopes.length; i++) {
			try {
				String domain = scopes[i] + ".";
				Name zone = Name.fromString(domain);
				
				Name name = Name.fromString("_" + serviceTypeID.getServices()[0] + "._" + serviceTypeID.getProtocols()[0], zone);
				
				long ttl = serviceInfo.getTTL();
				int priority = serviceInfo.getPriority();
				int port = serviceInfo.getLocation().getPort();
				String target = serviceInfo.getLocation().getHost();
				int weight = serviceInfo.getWeight();

				// TYPE.SRV
				Record record = Record.fromString(name, Type.SRV, DClass.IN, ttl, priority + " " + weight + " " + port + " " + target + ".", zone);
				Update update = new Update(zone);
				update.replace(record);
				
				// TYPE.TXT for service properties
				IServiceProperties properties = serviceInfo.getServiceProperties();
				Enumeration enumeration = properties.getPropertyNames();
				while(enumeration.hasMoreElements()) {
					Object property = enumeration.nextElement();
					String key = property.toString();
					String value = (String) properties.getProperty(key).toString();
					record = Record.fromString(name, Type.TXT, DClass.IN, ttl, key + "=" + value, zone);
					update.add(record);
				}
				
				// set up a new resolver for the given domain
				((SimpleResolver)resolver).setAddress(InetAddress.getByName("ns1.ecf-project.org"));
				resolver.setTCP(true);
				Message response = resolver.send(update);
				
				if(response.getRcode() != Rcode.NOERROR) {
					DnsSdDiscoveryException.getException(response.getRcode());
				}
			} catch (Exception e) {
				throw new DnsSdDiscoveryException(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.dnssd.DnsSdDiscoveryLocator#unregisterService(org.eclipse.ecf.discovery.IServiceInfo)
	 */
	public void unregisterService(IServiceInfo serviceInfo) {
		Assert.isNotNull(serviceInfo);
		DnsSdServiceID serviceID = (DnsSdServiceID) serviceInfo.getServiceID();
		DnsSdServiceTypeID serviceTypeID = (DnsSdServiceTypeID) serviceID.getServiceTypeID();
		String[] scopes = serviceTypeID.getScopes();
		for (int i = 0; i < scopes.length; i++) {
			try {
				String domain = scopes[i] + ".";
				Name zone = Name.fromString(domain);
				
				Name name = Name.fromString("_" + serviceTypeID.getServices()[0] + "._" + serviceTypeID.getProtocols()[0], zone);
				
				long ttl = serviceInfo.getTTL();
				int priority = serviceInfo.getPriority();
				int port = serviceInfo.getLocation().getPort();
				String target = serviceInfo.getLocation().getHost();
				int weight = serviceInfo.getWeight();

				// TYPE.SRV
				Record record = Record.fromString(name, Type.SRV, DClass.IN, ttl, priority + " " + weight + " " + port + " " + target + ".", zone);
				Update update = new Update(zone);
				update.delete(record);
				
				// TYPE.TXT for service properties
				IServiceProperties properties = serviceInfo.getServiceProperties();
				Enumeration enumeration = properties.getPropertyNames();
				while(enumeration.hasMoreElements()) {
					Object property = enumeration.nextElement();
					String key = property.toString();
					String value = (String) properties.getProperty(key).toString();
					record = Record.fromString(name, Type.TXT, DClass.IN, ttl, key + "=" + value, zone);
					update.delete(record);
				}
				
				// set up a new resolver for the given domain
				((SimpleResolver)resolver).setAddress(InetAddress.getByName("ns1.ecf-project.org"));
				resolver.setTCP(true);
				Message response = resolver.send(update);
				
				if(response.getRcode() != Rcode.NOERROR) {
					DnsSdDiscoveryException.getException(response.getRcode());
				}
			} catch (Exception e) {
				throw new DnsSdDiscoveryException(e);
			}
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
