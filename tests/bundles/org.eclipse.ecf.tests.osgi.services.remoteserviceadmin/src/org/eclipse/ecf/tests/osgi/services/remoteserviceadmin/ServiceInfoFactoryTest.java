/****************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.osgi.services.remoteserviceadmin;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.ServiceInfoFactory;

public abstract class ServiceInfoFactoryTest extends AbstractMetadataFactoryTest {

	protected void setUp() throws Exception {
		super.setUp();
		discoveryAdvertiser = getDiscoveryAdvertiser();
		Assert.isNotNull(discoveryAdvertiser);
		serviceInfoFactory = new ServiceInfoFactory();
	}
	
	public void testCreateServiceInfoFromMinimalEndpointDescription() throws Exception {
		IServiceInfo serviceInfo = createServiceInfoForDiscovery(createRequiredEndpointDescription());
		assertNotNull(serviceInfo);
	}
	
	public void testCreateServiceInfoFromFullEndpointDescription() throws Exception {
		IServiceInfo serviceInfo = createServiceInfoForDiscovery(createFullEndpointDescription());
		assertNotNull(serviceInfo);
	}

	public void testCreateBadOSGiEndpointDescription() throws Exception {
		try{
			createBadOSGiEndpointDescrption();
			fail();
		} catch (Exception e) {
			// this is test success
		}
	}
	
}
