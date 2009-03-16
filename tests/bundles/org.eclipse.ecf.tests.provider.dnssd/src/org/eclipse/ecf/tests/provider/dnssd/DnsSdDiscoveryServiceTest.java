/*******************************************************************************
 * Copyright (c) 2009 Markus Alexander Kuppe.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Alexander Kuppe (ecf-dev_eclipse.org <at> lemmster <dot> de) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.tests.provider.dnssd;

import java.net.URI;
import java.util.Properties;

import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.ServiceInfo;
import org.eclipse.ecf.discovery.ServiceProperties;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.discovery.identity.ServiceIDFactory;
import org.eclipse.ecf.provider.dnssd.DnsSdNamespace;
import org.eclipse.ecf.tests.discovery.DiscoveryServiceTest;

public class DnsSdDiscoveryServiceTest extends DiscoveryServiceTest {

	public DnsSdDiscoveryServiceTest() {
		this("ecf.discovery.dnssrv", "kuppe.org", "http", "tcp");
	}

	public DnsSdDiscoveryServiceTest(String string, String scopes,
			String service, String protocol) {
		super("ecf.discovery.dnssrv");
		setNamingAuthority("iana");
		setScope(scopes);
		setServices(new String[]{service});
		setProtocol(protocol);
		setComparator(new DnsSdDiscoveryComparator());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.DiscoveryServiceTest#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		final Properties props = new Properties();
		final URI uri = URI.create("http://www.kuppe.org:80/blog");
	
		Namespace namespace = discoveryLocator.getServicesNamespace();
		IServiceTypeID serviceTypeID = ServiceIDFactory.getDefault().createServiceTypeID(namespace, new String[]{"http"}, new String[]{"kuppe.org"}, new String[]{"tcp"}, "iana");
		assertNotNull(serviceTypeID);
		
		final ServiceProperties serviceProperties = new ServiceProperties(props);
		serviceProperties.setPropertyString("path", "/");
		serviceProperties.setPropertyString("dns-sd.ptcl", "http");

		serviceInfo = new ServiceInfo(uri, "www.kuppe.org", serviceTypeID, 10, 0, serviceProperties);
		assertNotNull(serviceInfo);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.DiscoveryServiceTest#getDiscoveryAdvertiser()
	 */
	protected IDiscoveryAdvertiser getDiscoveryAdvertiser() {
		return new IDiscoveryAdvertiser(){
		
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
			 */
			public Object getAdapter(Class adapter) {
				return null;
			}
		
			/* (non-Javadoc)
			 * @see org.eclipse.ecf.discovery.IDiscoveryAdvertiser#unregisterService(org.eclipse.ecf.discovery.IServiceInfo)
			 */
			public void unregisterService(IServiceInfo serviceInfo) {
			}
		
			/* (non-Javadoc)
			 * @see org.eclipse.ecf.discovery.IDiscoveryAdvertiser#unregisterAllServices()
			 */
			public void unregisterAllServices() {
			}
		
			/* (non-Javadoc)
			 * @see org.eclipse.ecf.discovery.IDiscoveryAdvertiser#registerService(org.eclipse.ecf.discovery.IServiceInfo)
			 */
			public void registerService(IServiceInfo serviceInfo) {
			}
		
			/* (non-Javadoc)
			 * @see org.eclipse.ecf.discovery.IDiscoveryAdvertiser#getServicesNamespace()
			 */
			public Namespace getServicesNamespace() {
				return new DnsSdNamespace();
			}
		};
	}
}
