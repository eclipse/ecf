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

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.discovery.IDiscoveryContainerAdapter;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.ServiceInfo;
import org.eclipse.ecf.discovery.ServiceProperties;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.identity.ServiceIDFactory;

import junit.framework.TestCase;

public class AbstractDiscoveryTest extends TestCase {

	protected IServiceInfo serviceInfo;
	protected IServiceInfo serviceInfo2;
	protected IServiceInfo serviceInfo3;
	protected String containerUnderTest;
	protected long waitTimeForProvider = 1000;
	protected Comparator comparator = new ServiceInfoComparator();
	protected String protocol = DiscoveryTestHelper.PROTOCOL;
	protected String scope = DiscoveryTestHelper.SCOPE;
	protected String namingAuthority = DiscoveryTestHelper.NAMINGAUTHORITY;
	protected IContainer container = null;
	protected IDiscoveryContainerAdapter discoveryContainer = null;
	protected int eventsToExpect = 1;
	
	public AbstractDiscoveryTest(String name) {
		super();
		this.containerUnderTest = name;
	}

	protected IDiscoveryContainerAdapter getAdapter(Class clazz) {
		final IDiscoveryContainerAdapter adapter = (IDiscoveryContainerAdapter) container.getAdapter(clazz);
		assertNotNull("Adapter must not be null", adapter);
		return adapter;
	}

	protected IContainer getContainer(String containerUnderTest) throws ContainerCreateException {
		return ContainerFactory.getDefault().createContainer(containerUnderTest);
	}

	protected IServiceID createServiceID(String serviceType, String serviceName)
			throws Exception {
				return ServiceIDFactory.getDefault().createServiceID(discoveryContainer.getServicesNamespace(), serviceType, serviceName);
			}

	protected void setWaitTimeForProvider(long aWaitTimeForProvider) {
		this.waitTimeForProvider = aWaitTimeForProvider + (aWaitTimeForProvider * 1 / 2);
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
	
		container = getContainer(containerUnderTest);
		discoveryContainer = getAdapter(IDiscoveryContainerAdapter.class);
		assertEquals("Container and DiscoveryContainer must be same", container, discoveryContainer);

		assertNotNull(container);
		assertNotNull(discoveryContainer);
	
		final Properties props = new Properties();
		final URI uri = DiscoveryTestHelper.createDefaultURI();
	
		IServiceID serviceID = (IServiceID) IDFactory.getDefault().createID(discoveryContainer.getServicesNamespace(), new Object[] {getServiceType(), DiscoveryTestHelper.getHost()});
		assertNotNull(serviceID);
		final ServiceProperties serviceProperties = new ServiceProperties(props);
		serviceProperties.setPropertyString(DiscoveryTest.class.getName() + "servicePropertiesString", "serviceProperties");
		serviceProperties.setProperty(DiscoveryTest.class.getName() + "servicePropertiesIntegerMax", new Integer(Integer.MIN_VALUE));
		serviceProperties.setProperty(DiscoveryTest.class.getName() + "servicePropertiesIntegerMin", new Integer(Integer.MAX_VALUE));
		serviceProperties.setProperty(DiscoveryTest.class.getName() + "servicePropertiesBoolean", new Boolean(false));
		serviceProperties.setPropertyBytes(DiscoveryTest.class.getName() + "servicePropertiesByte", new byte[]{-127, -126, -125, 0, 1, 2, 3, 'a', 'b', 'c', 'd', 126, 127});
		serviceInfo = new ServiceInfo(uri, serviceID, 1, 1, serviceProperties);
		assertNotNull(serviceInfo);
	
		IServiceID serviceID2 = (IServiceID) IDFactory.getDefault().createID(discoveryContainer.getServicesNamespace(), new Object[] {"_service._ecf._tests2._fooProtocol.fooScope._fooNA", DiscoveryTestHelper.getHost()});
		assertNotNull(serviceID);
		final ServiceProperties serviceProperties2 = new ServiceProperties(props);
		serviceProperties2.setPropertyString("serviceProperties2", "serviceProperties2");
		serviceInfo2 = new ServiceInfo(uri, serviceID2, 2, 2, serviceProperties2);
		assertNotNull(serviceInfo2);
	
		IServiceID serviceID3 = (IServiceID) IDFactory.getDefault().createID(discoveryContainer.getServicesNamespace(), new Object[] {"_service._ecf._tests3._barProtocol.barScope._barNA", DiscoveryTestHelper.getHost()});
		assertNotNull(serviceID);
		final ServiceProperties serviceProperties3 = new ServiceProperties(props);
		serviceProperties3.setPropertyString("serviceProperties3", "serviceProperties3");
		serviceInfo3 = new ServiceInfo(uri, serviceID3, 3, 3, serviceProperties3);
		assertNotNull(serviceInfo3);
	}

}
