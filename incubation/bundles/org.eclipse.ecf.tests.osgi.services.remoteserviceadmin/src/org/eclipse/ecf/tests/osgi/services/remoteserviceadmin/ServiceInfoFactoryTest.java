/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.tests.osgi.services.remoteserviceadmin;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.DefaultServiceInfoFactory;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.IServiceInfoFactory;

public class ServiceInfoFactoryTest extends AbstractMetadataFactoryTest {

	protected void setUp() throws Exception {
		super.setUp();
		discoveryAdvertiser = getDiscoveryAdvertiser();
		Assert.isNotNull(discoveryAdvertiser);
	}
	
	public void testCreateServiceInfoFromMinimalEndpointDescription() throws Exception {
		IServiceInfoFactory serviceInfoFactory = new DefaultServiceInfoFactory();
		IServiceInfo serviceInfo = serviceInfoFactory.createServiceInfoForDiscovery(discoveryAdvertiser, createRequiredEndpointDescription());
		assertNotNull(serviceInfo);
	}
}
