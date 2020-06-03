/****************************************************************************
 * Copyright (c) 2009, 2010 REMAIN B.V. The Netherlands. (http://www.remainsoftware.com).
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 *  Contributors:
 *     A. Aadel - initial API and implementation     
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.provider.zookeeper;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.eclipse.ecf.discovery.IDiscoveryLocator;
import org.eclipse.ecf.provider.zookeeper.core.ZooDiscoveryContainerInstantiator;
import org.eclipse.ecf.tests.discovery.DiscoveryTest;

public class ZooDiscoveryTest extends DiscoveryTest {

	private IContainer container = null;

	public ZooDiscoveryTest() {
		super(ZooDiscoveryContainerInstantiator.NAME);
	}

	protected void setUp() throws Exception {
		this.container = ContainerFactory.getDefault().createContainer(
				ZooDiscoveryContainerInstantiator.NAME);
		assertNotNull(this.container);
		super.setUp();
	}

	protected IDiscoveryLocator getDiscoveryLocator() {
		final IDiscoveryLocator adapter = (IDiscoveryLocator) this.container
				.getAdapter(IDiscoveryLocator.class);
		assertNotNull("Adapter must not be null", adapter); //$NON-NLS-1$
		return adapter;
	}

	protected IDiscoveryAdvertiser getDiscoveryAdvertiser() {
		final IDiscoveryAdvertiser adapter = (IDiscoveryAdvertiser) this.container
				.getAdapter(IDiscoveryAdvertiser.class);
		assertNotNull("Adapter must not be null", adapter); //$NON-NLS-1$
		return adapter;
	}
}
