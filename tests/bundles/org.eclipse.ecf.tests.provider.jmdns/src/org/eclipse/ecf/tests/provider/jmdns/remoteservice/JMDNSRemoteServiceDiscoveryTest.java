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

import java.net.URI;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.discovery.IContainerServiceInfoAdapter;
import org.eclipse.ecf.discovery.IDiscoveryContainerAdapter;
import org.eclipse.ecf.discovery.IServiceEvent;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.IServiceListener;
import org.eclipse.ecf.discovery.ServiceInfo;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.tests.discovery.DiscoveryTest;

/**
 *
 */
public class JMDNSRemoteServiceDiscoveryTest extends DiscoveryTest {

	private static final String JMDNS_CONTAINER_NAME = "ecf.discovery.jmdns";
	final String SCOPE = "local";
	final String NAMINGAUTHORITY = "IANA";
	final String PROTOCOL = "tcp";

	final String SERVICE_TYPE = "_osgi._ecftcp._" + PROTOCOL + "." + SCOPE + "._" + NAMINGAUTHORITY;

	final int SERVER_PORT = 3333;

	IServiceInfo serviceInfo;

	public JMDNSRemoteServiceDiscoveryTest() {
		super(JMDNS_CONTAINER_NAME);
	}

	protected void setUp() throws Exception {
		super.setUp();
		container = ContainerFactory.getDefault().createContainer(containerUnderTest);
		discoveryContainer = (IDiscoveryContainerAdapter) container.getAdapter(IDiscoveryContainerAdapter.class);
		final ServiceInfo svcInfo = new ServiceInfo(createDefaultURI(), createServiceID(SERVICE_TYPE, "JMDNSRemoteServiceDiscoveryTest" + System.currentTimeMillis()));
		svcInfo.setContainerProperties("ecf.generic.client", "ecftcp", "server", null);
		serviceInfo = svcInfo;
	}

	public void testRegisterOSGiService() throws Exception {
		testConnect();
		discoveryContainer.addServiceListener(createServiceListener());
		registerService(serviceInfo);
		Thread.sleep(40000);
	}

	/**
	 * @return
	 */
	private IServiceListener createServiceListener() {
		return new IServiceListener() {

			public void serviceDiscovered(IServiceEvent anEvent) {
				System.out.println("serviceDiscovered(" + anEvent + ")");
				final IServiceInfo serviceInfo = anEvent.getServiceInfo();
				final URI location = serviceInfo.getLocation();
				System.out.println("location=" + location);
				final IServiceID serviceID = serviceInfo.getServiceID();
				System.out.println("serviceID=" + serviceID);
				final IServiceTypeID serviceTypeID = serviceID.getServiceTypeID();
				System.out.println("serviceTypeID=" + serviceTypeID);
				final IContainerServiceInfoAdapter adapter = (IContainerServiceInfoAdapter) serviceInfo.getAdapter(IContainerServiceInfoAdapter.class);
				if (adapter != null) {
					/** 
					 connect here!
					try {
						final IContainer clientContainer = ContainerFactory.getDefault().createContainer(adapter.getContainerFactoryName());
						final String target = adapter.getTarget();
						clientContainer.connect(IDFactory.getDefault().createID(clientContainer.getConnectNamespace(), target), null);
					} catch (final Exception e) {
						e.printStackTrace();
					}
					*/
				}
			}

			public void serviceUndiscovered(IServiceEvent anEvent) {
				// TODO Auto-generated method stub

			}
		};
	}
}
