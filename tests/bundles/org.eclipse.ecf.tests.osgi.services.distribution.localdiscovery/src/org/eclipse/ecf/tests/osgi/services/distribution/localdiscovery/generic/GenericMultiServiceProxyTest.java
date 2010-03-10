/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.tests.osgi.services.distribution.localdiscovery.generic;

import java.net.URL;

import org.eclipse.ecf.osgi.services.distribution.IDistributionConstants;
import org.eclipse.ecf.tests.internal.osgi.services.distribution.localdiscovery.Activator;
import org.eclipse.ecf.tests.osgi.services.distribution.TestServiceInterface1;
import org.eclipse.ecf.tests.osgi.services.distribution.localdiscovery.AbstractMultiServiceProxyTest;
import org.eclipse.ecf.tests.remoteservice.IConcatService;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

// NOTE:  running this test successfully depends upon having a GenericMultiServiceHost
// server running
public class GenericMultiServiceProxyTest extends AbstractMultiServiceProxyTest
		implements IDistributionConstants {

	private static final String SERVICE_DESCRIPTION_FILE = "/genericmultiservicehost.xml";

	protected URL getServiceDescriptionURL() {
		return Activator.getDefault().getContext().getBundle()
		.getEntry(SERVICE_DESCRIPTION_FILE);
	}
	
	protected String getClientContainerName() {
		return "ecf.generic.client";
	}

	public void testTestServiceInterface1Access() throws Exception {
		publishServiceDescriptions(serviceDescriptionURL);
		sleep(5000);
		String classname = TestServiceInterface1.class.getName();
		// Setup service tracker for client container
		ServiceTracker st = createProxyServiceTracker(classname);
		// Service Consumer - Get (remote) service references
		ServiceReference[] remoteReferences = st.getServiceReferences();
		assertReferencesValidAndFirstHasCorrectType(remoteReferences, classname);
		// Spec requires that the 'service.imported' property be set
		assertTrue(remoteReferences[0].getProperty(SERVICE_IMPORTED) != null);
		st.close();
	}
	
	public void testConcatServiceInterfaceAccess() throws Exception {
		publishServiceDescriptions(serviceDescriptionURL);
		sleep(5000);
		String classname = IConcatService.class.getName();
		// Setup service tracker for client container
		ServiceTracker st = createProxyServiceTracker(classname);
		// Service Consumer - Get (remote) service references
		ServiceReference[] remoteReferences = st.getServiceReferences();
		assertReferencesValidAndFirstHasCorrectType(remoteReferences, classname);
		// Spec requires that the 'service.imported' property be set
		assertTrue(remoteReferences[0].getProperty(SERVICE_IMPORTED) != null);
		st.close();
	}

}
