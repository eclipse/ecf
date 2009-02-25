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
package org.eclipse.ecf.tests.discovery;

import java.net.URI;
import java.util.Comparator;
import java.util.Properties;
import java.util.Random;

import junit.framework.TestCase;

import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.eclipse.ecf.discovery.IDiscoveryLocator;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.ServiceInfo;
import org.eclipse.ecf.discovery.ServiceProperties;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.identity.ServiceIDFactory;

public abstract class AbstractDiscoveryTest extends TestCase {
	private final Random random;

	protected IServiceInfo serviceInfo;
	protected String protocol = DiscoveryTestHelper.PROTOCOL;
	protected String scope = DiscoveryTestHelper.SCOPE;
	protected String namingAuthority = DiscoveryTestHelper.NAMINGAUTHORITY;
	protected Comparator comparator = new ServiceInfoComparator();

	protected String containerUnderTest;
	protected IDiscoveryLocator discoveryLocator = null;
	protected IDiscoveryAdvertiser discoveryAdvertiser = null;
	
	public AbstractDiscoveryTest(String name) {
		super();
		this.containerUnderTest = name;
		this.random = new Random();
	}

	protected abstract IDiscoveryLocator getDiscoveryLocator();

	protected abstract IDiscoveryAdvertiser getDiscoveryAdvertiser();

	protected IServiceID createServiceID(String serviceType, String serviceName)
			throws Exception {
				return ServiceIDFactory.getDefault().createServiceID(discoveryLocator.getServicesNamespace(), serviceType, serviceName);
			}

	protected void setComparator(Comparator comparator) {
		this.comparator = comparator;
	}

	protected void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	protected void setScope(String scope) {
		this.scope = scope;
	}

	protected void setNamingAuthority(String namingAuthority) {
		this.namingAuthority = namingAuthority;
	}

	protected String getServiceType() {
		return "_" + DiscoveryTestHelper.SERVICES[0] + "._" + DiscoveryTestHelper.SERVICES[1] + "._" + DiscoveryTestHelper.SERVICES[2] + "._" + protocol + "." + scope + "._" + namingAuthority;
	}

	protected void setUp() throws Exception {
		super.setUp();
		assertNotNull(containerUnderTest);
		assertTrue(containerUnderTest.startsWith("ecf.discovery."));
	
		discoveryLocator = getDiscoveryLocator();
		discoveryAdvertiser = getDiscoveryAdvertiser();
		assertNotNull(discoveryLocator);
		assertNotNull(discoveryAdvertiser);
	
		final Properties props = new Properties();
		final URI uri = DiscoveryTestHelper.createDefaultURI();
	
		IServiceID serviceID = (IServiceID) IDFactory.getDefault().createID(discoveryLocator.getServicesNamespace(), new Object[] {getServiceType(), DiscoveryTestHelper.getHost()});
		assertNotNull(serviceID);
		final ServiceProperties serviceProperties = new ServiceProperties(props);
		serviceProperties.setPropertyString(getClass() + "testIdentifier", Long.toString(random.nextLong()));
		serviceProperties.setPropertyString(getName() + "servicePropertiesString", "serviceProperties");
		serviceProperties.setProperty(getName() + "servicePropertiesIntegerMax", new Integer(Integer.MIN_VALUE));
		serviceProperties.setProperty(getName() + "servicePropertiesIntegerMin", new Integer(Integer.MAX_VALUE));
		serviceProperties.setProperty(getName() + "servicePropertiesBoolean", new Boolean(false));
		serviceProperties.setPropertyBytes(getName() + "servicePropertiesByte", new byte[]{-127, -126, -125, 0, 1, 2, 3, 'a', 'b', 'c', 'd', 126, 127});
		serviceInfo = new ServiceInfo(uri, serviceID, 1, 1, serviceProperties);
		assertNotNull(serviceInfo);
	}

}
