/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.tests.provider.jmdns.remoteservice;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.discovery.IDiscoveryContainerAdapter;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.IServiceProperties;
import org.eclipse.ecf.tests.discovery.DiscoveryTest;

/**
 *
 */
public class JMDNSRemoteServiceDiscoveryTest extends DiscoveryTest {

	private static final String JMDNS_CONTAINER_NAME = "ecf.discovery.jmdns";
	public final String SCOPE = "local";
	public final String NAMINGAUTHORITY = "IANA";
	public final String PROTOCOL = "tcp";

	public final String SERVICE_TYPE = "_osgi._ecftcp._" + PROTOCOL + "." + SCOPE + "._" + NAMINGAUTHORITY;

	IServiceInfo serviceInfo;

	public JMDNSRemoteServiceDiscoveryTest() {
		super(JMDNS_CONTAINER_NAME, 1000);
	}

	protected void setUp() throws Exception {
		super.setUp();
		container = ContainerFactory.getDefault().createContainer(containerUnderTest);
		discoveryContainer = (IDiscoveryContainerAdapter) container.getAdapter(IDiscoveryContainerAdapter.class);
		final IServiceProperties serviceProperties = createServiceProperties();
		serviceProperties.setPropertyString("containerTypeName", "ecf.generic.client");
		serviceProperties.setPropertyString("connectProtocol", "ecftcp");
		serviceProperties.setPropertyString("connectUser", "guest");
		serviceProperties.setPropertyString("requiresPassword", "false");
		serviceProperties.setPropertyString("connectPath", "/server");
		serviceInfo = createServiceInfo(createDefaultURI(), createServiceID(SERVICE_TYPE, "JMDNSRemoteServiceDiscoveryTest" + System.currentTimeMillis()), serviceProperties);
	}

	public void testRegisterOSGiService() throws Exception {
		testConnect();
		registerService(serviceInfo);
		Thread.sleep(15000);
	}
}
