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

package org.eclipse.ecf.tests.discovery;

import java.net.InetAddress;
import java.net.URI;
import java.util.Comparator;

import junit.framework.TestCase;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.discovery.IDiscoveryContainerAdapter;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.identity.ServiceIDFactory;

/**
 *
 */
public abstract class AbstractDiscoveryTest extends TestCase {

	protected IContainer container = null;
	protected IDiscoveryContainerAdapter discoveryContainer = null;
	protected String containerUnderTest;
	protected long waitTimeForProvider;
	protected Comparator comparator;

	/**
	 * 
	 */
	public AbstractDiscoveryTest() {
		super();
	}

	/**
	 * @param name
	 */
	public AbstractDiscoveryTest(String name) {
		super(name);
	}

	protected IDiscoveryContainerAdapter getAdapter(Class clazz) {
		final IDiscoveryContainerAdapter adapter = (IDiscoveryContainerAdapter) container.getAdapter(clazz);
		assertNotNull("Adapter must not be null", adapter);
		return adapter;
	}

	protected IServiceID createServiceID(String serviceType, String serviceName) throws Exception {
		return ServiceIDFactory.getDefault().createServiceID(discoveryContainer.getServicesNamespace(), serviceType, serviceName);
	}

	protected URI createURI(String uri) throws Exception {
		return new URI(uri);
	}

	protected String getAuthority() throws Exception {
		return System.getProperty("user.name") + "@" + InetAddress.getLocalHost().getHostAddress();
	}

	protected int getPort() {
		return ITestConstants.PORT;
	}

	protected URI createDefaultURI() throws Exception {
		return createURI("foo://" + getAuthority() + ":" + getPort() + "/");
	}

	protected void registerService(IServiceInfo serviceInfo) throws Exception {
		assertNotNull(serviceInfo);
		assertNotNull(discoveryContainer);
		discoveryContainer.registerService(serviceInfo);
	}

	protected void unregisterService(IServiceInfo serviceInfo) throws Exception {
		assertNotNull(serviceInfo);
		assertNotNull(discoveryContainer);
		discoveryContainer.unregisterService(serviceInfo);
	}

}